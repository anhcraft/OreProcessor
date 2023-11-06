package dev.anhcraft.oreprocessor.storage.player;

import com.google.common.base.Preconditions;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.event.AsyncPlayerDataLoadEvent;
import dev.anhcraft.oreprocessor.storage.player.db.DatabaseManager;
import dev.anhcraft.oreprocessor.storage.player.flatfile.FlatFileManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class PlayerDataManager implements Listener {
    private final static long EXPIRATION_TIME = Duration.ofMinutes(5).toMillis();
    private final OreProcessor plugin;
    private final Map<UUID, TrackedPlayerData> playerDataMap = new HashMap<>();
    private final Object LOCK = new Object();
    private final IPlayerDataStorage storage;

    public PlayerDataManager(OreProcessor plugin) {
        this.plugin = plugin;

        switch (plugin.storageConfig.type) {
            case FLATFILE:
                File folder = new File(plugin.getDataFolder(), "data/players");
                folder.mkdirs();
                storage = new FlatFileManager(plugin, folder);
                break;
            case DATABASE:
                storage = new DatabaseManager(plugin, plugin.storageConfig.playerDataTable, plugin.storageConfig.createDataSource());
                break;
            default:
                throw new RuntimeException("Unknown storage type: " + plugin.storageConfig.type);
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        try {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                requireData(player.getUniqueId()).get();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::checkTask, 20, 200);
    }

    public void streamData(BiConsumer<UUID, PlayerData> consumer) {
        synchronized (LOCK) {
            for (Map.Entry<UUID, TrackedPlayerData> e : playerDataMap.entrySet()) {
                consumer.accept(e.getKey(), e.getValue().getPlayerData());
            }
        }
    }

    @NotNull
    public Optional<PlayerData> getData(@NotNull UUID uuid) {
        synchronized (LOCK) {
            return Optional.ofNullable(playerDataMap.get(uuid)).map(TrackedPlayerData::getPlayerData);
        }
    }

    @NotNull
    public PlayerData getData(@NotNull Player player) {
        Preconditions.checkArgument(player.isOnline(), "Player must be online");

        synchronized (LOCK) {
            return Objects.requireNonNull(playerDataMap.get(player.getUniqueId())).getPlayerData();
        }
    }

    @NotNull
    public CompletableFuture<PlayerData> requireData(@NotNull UUID uuid) {
        synchronized (LOCK) {
            if (playerDataMap.containsKey(uuid)) {
                return CompletableFuture.completedFuture(playerDataMap.get(uuid).getPlayerData());
            } else {
                return CompletableFuture.supplyAsync(() -> {
                    PlayerData playerData = storage.loadOrCreate(uuid);
                    synchronized (LOCK) { // code is now async
                        Bukkit.getPluginManager().callEvent(new AsyncPlayerDataLoadEvent(uuid, playerData));
                        TrackedPlayerData trackedPlayerData = new TrackedPlayerData(playerData, System.currentTimeMillis());
                        if (Bukkit.getPlayer(uuid) == null) {
                            trackedPlayerData.setShortTerm();
                            plugin.debug("%s's data loaded, set to short-term", uuid);
                        } else {
                            trackedPlayerData.setLongTerm();
                            plugin.debug("%s's data loaded, set to long-term", uuid);
                        }
                        playerDataMap.put(uuid, trackedPlayerData);
                    }
                    return playerData;
                });
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerJoin(PlayerJoinEvent event) {
        synchronized (LOCK) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (playerDataMap.containsKey(uuid)) {
                plugin.debug("%s's data changed: ? term → long term", uuid);
                playerDataMap.get(uuid).setLongTerm();
            } else {
                PlayerData playerData = storage.loadOrCreate(uuid);
                CompletableFuture.runAsync(() -> {
                    Bukkit.getPluginManager().callEvent(new AsyncPlayerDataLoadEvent(uuid, playerData));
                });
                TrackedPlayerData trackedPlayerData = new TrackedPlayerData(playerData, System.currentTimeMillis());
                trackedPlayerData.setLongTerm();
                playerDataMap.put(uuid, trackedPlayerData);
                plugin.debug("%s's data loaded, set to long-term", uuid);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerQuit(PlayerQuitEvent event) {
        synchronized (LOCK) {
            UUID uuid = event.getPlayer().getUniqueId();
            if (playerDataMap.containsKey(uuid)) {
                plugin.debug("%s's data changed: ? term → short term", uuid);
                playerDataMap.get(uuid).setShortTerm();
            }
        }
    }

    private void checkTask() {
        synchronized (LOCK) {
            for (Iterator<Map.Entry<UUID, TrackedPlayerData>> it = playerDataMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, TrackedPlayerData> entry = it.next();
                PlayerData playerData = entry.getValue().getPlayerData();
                boolean toRemove = entry.getValue().isShortTerm() && System.currentTimeMillis() - entry.getValue().getLoadTime() > EXPIRATION_TIME;

                if (toRemove) {
                    playerData.setHibernationStart(System.currentTimeMillis());
                }

                if (storage instanceof FlatFileManager) {
                    ((FlatFileManager) storage).save(entry.getKey(), playerData);
                }

                if (toRemove) {
                    it.remove();
                    plugin.debug("%s's data now expires", entry.getKey());
                }
            }
        }
    }

    public void terminate() {
        synchronized (LOCK) {
            for (Iterator<Map.Entry<UUID, TrackedPlayerData>> it = playerDataMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<UUID, TrackedPlayerData> entry = it.next();
                PlayerData playerData = entry.getValue().getPlayerData();
                playerData.setHibernationStart(System.currentTimeMillis());
                if (storage instanceof FlatFileManager) {
                    ((FlatFileManager) storage).save(entry.getKey(), playerData);
                }
                it.remove();
            }
        }
    }
}
