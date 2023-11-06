package dev.anhcraft.oreprocessor.storage.player.flatfile;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.storage.player.IPlayerDataStorage;
import dev.anhcraft.oreprocessor.storage.player.flatfile.converter.PlayerDataConverter;
import dev.anhcraft.oreprocessor.storage.player.flatfile.model.GenericPlayerDataConfig;
import dev.anhcraft.oreprocessor.storage.player.flatfile.model.PlayerDataConfigV0;
import dev.anhcraft.oreprocessor.storage.player.flatfile.model.PlayerDataConfigV1;
import dev.anhcraft.oreprocessor.util.CompressUtils;
import dev.anhcraft.oreprocessor.util.ConfigHelper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.UUID;

public class FlatFileManager implements IPlayerDataStorage {
    private final OreProcessor plugin;
    private final File folder;

    public FlatFileManager(OreProcessor plugin, File folder) {
        this.plugin = plugin;
        this.folder = folder;
    }

    private PlayerDataConfigV1 load(UUID uuid) {
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

    public void save(UUID uuid, PlayerData data) {
        PlayerDataConfigV1 playerData = ((FlatPlayerData) data).internal();
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

    @Override
    public PlayerData loadOrCreate(UUID id) {
        return new FlatPlayerData(load(id));
    }
}
