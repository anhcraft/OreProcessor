package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import org.bukkit.scheduler.BukkitRunnable;

public class MineralProcessingTask extends BukkitRunnable {
    @Override
    public void run() {
        OreProcessor.getInstance().playerDataManager.streamData(PlayerData::processOre);
    }
}
