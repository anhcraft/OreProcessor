package dev.anhcraft.oreprocessor.storage;

import com.google.common.base.Preconditions;
import dev.anhcraft.jvmkit.utils.PresentPair;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.data.PlayerDataConfig;
import dev.anhcraft.oreprocessor.util.ConfigHelper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class PlayerDataManager implements Listener {
    private final static long EXPIRATION_TIME = Duration.ofMinutes(5).toMillis();
    private final OreProcessor plugin;
    private final Map<UUID, TrackedPlayerData> playerDataMap = new HashMap<>();
    private final Object LOCK = new Object();
    private final File folder;
    private Consumer<PresentPair<UUID, PlayerDataConfig>> onPlayerDataLoad;

    public PlayerDataManager(OreProcessor plugin) {
        this.plugin = plugin;
        folder = new File(plugin.getDataFolder(), "data");
        folder.mkdir();

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

    public void setOnPlayerDataLoad(Consumer<PresentPair<UUID, PlayerDataConfig>> onPlayerDataLoad) {
        if (this.onPlayerDataLoad != null) {
            throw new IllegalStateException("onPlayerDataLoad already set");
        }
        this.onPlayerDataLoad = onPlayerDataLoad;
    }

    @NotNull
    private PlayerDataConfig loadData(UUID uuid) {
        File file = new File(folder, uuid + ".yml");
        if (file.exists()) {
            return Objects.requireNonNull(ConfigHelper.load(PlayerDataConfig.class, YamlConfiguration.loadConfiguration(file)));
        } else {
            // If data not exists, don't create file
            return new PlayerDataConfig();
        }
    }

    private void saveDataIfDirty(UUID uuid, @NotNull PlayerDataConfig playerData) {
        if (playerData.dirty.compareAndSet(true, false)) {
            plugin.debug("Saving %s's data...", uuid);
            File file = new File(folder, uuid + ".yml");
            YamlConfiguration conf = new YamlConfiguration();
            ConfigHelper.save(PlayerDataConfig.class, conf, playerData);
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void streamData(Consumer<PlayerDataConfig> consumer) {
        synchronized (LOCK) {
            for (TrackedPlayerData tpd : playerDataMap.values()) {
                consumer.accept(tpd.getPlayerData());
            }
        }
    }

    @NotNull
    public Optional<PlayerDataConfig> getData(@NotNull UUID uuid) {
        synchronized (LOCK) {
            return Optional.ofNullable(playerDataMap.get(uuid)).map(TrackedPlayerData::getPlayerData);
        }
    }

    @NotNull
    public PlayerDataConfig getData(@NotNull Player player) {
        Preconditions.checkArgument(player.isOnline(), "Player must be online");

        synchronized (LOCK) {
            return Objects.requireNonNull(playerDataMap.get(player.getUniqueId())).getPlayerData();
        }
    }

    @NotNull
    public CompletableFuture<PlayerDataConfig> requireData(@NotNull UUID uuid) {
        synchronized (LOCK) {
            if (playerDataMap.containsKey(uuid)) {
                return CompletableFuture.completedFuture(playerDataMap.get(uuid).getPlayerData());
            } else {
                return CompletableFuture.supplyAsync(() -> {
                    PlayerDataConfig playerData = loadData(uuid);
                    synchronized (LOCK) { // code is now async
                        if (onPlayerDataLoad != null) onPlayerDataLoad.accept(new PresentPair<>(uuid, playerData));
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
                PlayerDataConfig playerData = loadData(uuid);
                if (onPlayerDataLoad != null) onPlayerDataLoad.accept(new PresentPair<>(uuid, playerData));
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
                PlayerDataConfig playerData = entry.getValue().getPlayerData();
                boolean toRemove = entry.getValue().isShortTerm() && System.currentTimeMillis() - entry.getValue().getLoadTime() > EXPIRATION_TIME;

                if (toRemove) {
                    playerData.hibernationStart = System.currentTimeMillis();
                    playerData.markDirty();
                }

                saveDataIfDirty(entry.getKey(), playerData);

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
                PlayerDataConfig playerData = entry.getValue().getPlayerData();
                playerData.hibernationStart = System.currentTimeMillis();
                playerData.markDirty();
                saveDataIfDirty(entry.getKey(), playerData);
                it.remove();
            }
        }
    }
}
