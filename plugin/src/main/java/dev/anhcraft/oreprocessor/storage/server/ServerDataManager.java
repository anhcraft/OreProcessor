package dev.anhcraft.oreprocessor.storage.server;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.util.CompressUtils;
import dev.anhcraft.oreprocessor.util.ConfigHelper;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

public class ServerDataManager {
    private final OreProcessor plugin;
    private final File file;
    private ServerDataImpl serverData;

    public ServerDataManager(OreProcessor plugin) {
        this.plugin = plugin;
        File folder = new File(plugin.getDataFolder(), "data");
        folder.mkdir();
        file = new File(folder, "server.gz");
    }

    public ServerDataImpl getData() {
        return serverData;
    }

    public void loadData() {
        if (file.exists()) {
            YamlConfiguration conf = null;
            try {
                conf = YamlConfiguration.loadConfiguration(new StringReader(CompressUtils.readAndDecompressString(file)));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conf == null)
                serverData = new ServerDataImpl(new ServerDataConfig());

            serverData = new ServerDataImpl(ConfigHelper.load(ServerDataConfig.class, conf));
        } else {
            serverData = new ServerDataImpl(new ServerDataConfig());
        }

        plugin.debug("Server data loaded!");

        if (plugin.mainConfig.purgeStats.maxServerRecords > 0) {
            OreProcessor.getInstance().debug(String.format(
                    "Removed %d oldest statistics records from server data",
                    serverData.internal().getStats().purgeHourlyStats(plugin.mainConfig.purgeStats.maxServerRecords)
            ));
        }
    }

    private void saveDataIfDirty() {
        if (serverData.internal().dirty.compareAndSet(true, false)) {
            plugin.debug("Saving server data...");
            YamlConfiguration conf = new YamlConfiguration();
            ConfigHelper.save(ServerDataConfig.class, conf, serverData.internal());
            try {
                CompressUtils.compressAndWriteString(conf.saveToString(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::saveDataIfDirty, 20, 200);
    }

    public void terminate() {
        serverData.internal().dirty.set(true);
        saveDataIfDirty();
    }
}
