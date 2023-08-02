package dev.anhcraft.oreprocessor.storage.player;

import com.google.common.base.Preconditions;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.event.AsyncPlayerDataLoadEvent;
import dev.anhcraft.oreprocessor.storage.player.compat.GenericPlayerDataConfig;
import dev.anhcraft.oreprocessor.storage.player.compat.PlayerDataConfigV0;
import dev.anhcraft.oreprocessor.util.CompressUtils;
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
import java.io.StringReader;
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
    private final File folder;

    public PlayerDataManager(OreProcessor plugin) {
        this.plugin = plugin;
        folder = new File(plugin.getDataFolder(), "data/players");
        folder.mkdirs();

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

    @NotNull
    private PlayerDataConfigV1 loadData(UUID uuid) {
        File file = new File(folder, uuid + ".gz");
        if (file.exists()) {
            YamlConfiguration conf = null;
            try {
                conf = YamlConfiguration.loadConfiguration(new StringReader(CompressUtils.readAndDecompressString(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conf == null)
                return new PlayerDataConfigV1();

            // attempt to check version first
            GenericPlayerDataConfig genericData = ConfigHelper.load(GenericPlayerDataConfig.class, conf);
            if (genericData.dataVersion > OreProcessor.LATEST_PLAYER_DATA_VERSION) {
                plugin.getLogger().severe(String.format(
                        "Attempting to load player data '%s' from newer version (v%d) while maximum supported version is v%d",
                        uuid, genericData.dataVersion, OreProcessor.LATEST_PLAYER_DATA_VERSION
                ));
                throw new RuntimeException();
            }

            if (genericData.dataVersion < 0) {
                plugin.getLogger().severe(String.format(
                        "Player data '%s' is broken as its version is v%d (while minimum is v0)",
                        uuid, genericData.dataVersion
                ));
                throw new RuntimeException();
            }

            // --start: old data upgrade--
            if (genericData.dataVersion == 0) {
                PlayerDataConfigV0 playerData = ConfigHelper.load(PlayerDataConfigV0.class, conf);
                genericData = PlayerDataConverter.convert(playerData); // data is now V1
                plugin.debug("Player data '%s' has been upgraded to V1!", uuid);
            }
            // --end--

            // If the version is already latest or went through all data upgrades successfully
            if (genericData.dataVersion == OreProcessor.LATEST_PLAYER_DATA_VERSION) {
                genericData = ConfigHelper.load(PlayerDataConfigV1.class, conf);
            }

            return (PlayerDataConfigV1) genericData;
        } else {
            // If data not exists, don't create file (it is unneeded)
            return new PlayerDataConfigV1();
        }
    }

    private void saveDataIfDirty(UUID uuid, @NotNull PlayerDataConfigV1 playerData) {
        if (playerData.dirty.compareAndSet(true, false)) {
            plugin.debug("Saving %s's data...", uuid);
            File file = new File(folder, uuid + ".gz");
            YamlConfiguration conf = new YamlConfiguration();
            ConfigHelper.save(PlayerDataConfigV1.class, conf, playerData);
            try {
                CompressUtils.compressAndWriteString(conf.saveToString(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                    PlayerDataImpl playerData = new PlayerDataImpl(loadData(uuid));
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
                PlayerDataImpl playerData = new PlayerDataImpl(loadData(uuid));
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
                PlayerDataImpl playerData = entry.getValue().getPlayerData();
                boolean toRemove = entry.getValue().isShortTerm() && System.currentTimeMillis() - entry.getValue().getLoadTime() > EXPIRATION_TIME;

                if (toRemove) {
                    playerData.internal().hibernationStart = System.currentTimeMillis();
                    playerData.internal().markDirty();
                }

                saveDataIfDirty(entry.getKey(), playerData.internal());

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
                PlayerDataImpl playerData = entry.getValue().getPlayerData();
                playerData.internal().hibernationStart = System.currentTimeMillis();
                playerData.internal().markDirty();
                saveDataIfDirty(entry.getKey(), playerData.internal());
                it.remove();
            }
        }
    }
}
