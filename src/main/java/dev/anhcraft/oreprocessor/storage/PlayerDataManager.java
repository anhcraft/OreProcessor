package dev.anhcraft.oreprocessor.storage;

import com.google.common.base.Preconditions;
import dev.anhcraft.oreprocessor.OreProcessor;
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

public class PlayerDataManager implements Listener {
    private final static long EXPIRATION_TIME = Duration.ofMinutes(5).toMillis();
    private final OreProcessor plugin;
    private final Map<UUID, TrackedPlayerData> playerDataMap = new HashMap<>();
    private final Object LOCK = new Object();
    private final File folder;

    public PlayerDataManager(OreProcessor plugin) {
        this.plugin = plugin;
        folder = new File(plugin.getDataFolder(), "data");
        folder.mkdir();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::checkTask, 20, 200);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            requireData(player.getUniqueId());
        }
    }

    @NotNull
    private PlayerData loadData(UUID uuid) {
        File file = new File(folder, uuid + ".yml");
        if (file.exists()) {
            return Objects.requireNonNull(ConfigHelper.load(PlayerData.class, YamlConfiguration.loadConfiguration(file)));
        } else {
            // If data not exists, don't create file
            return new PlayerData();
        }
    }

    private void saveDataIfDirty(UUID uuid, @NotNull PlayerData playerData) {
        if (playerData.dirty.compareAndSet(true, false)) {
            plugin.debug("Saving %s's data...", uuid);
            File file = new File(folder, uuid + ".yml");
            YamlConfiguration conf = new YamlConfiguration();
            ConfigHelper.save(PlayerData.class, conf, playerData);
            try {
                conf.save(file);
            } catch (IOException e) {
                e.printStackTrace();
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
                    PlayerData playerData = loadData(uuid);
                    synchronized (LOCK) { // code is now async
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
                PlayerData playerData = loadData(uuid);
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
                saveDataIfDirty(entry.getKey(), entry.getValue().getPlayerData());

                if (entry.getValue().isShortTerm() && System.currentTimeMillis() - entry.getValue().getLoadTime() >  EXPIRATION_TIME) {
                    it.remove();
                    plugin.debug("%s's data now expires", entry.getKey());
                }
            }
        }
    }
}
