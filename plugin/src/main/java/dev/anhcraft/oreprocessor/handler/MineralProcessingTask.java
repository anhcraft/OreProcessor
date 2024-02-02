package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class MineralProcessingTask extends BukkitRunnable {
    @Override
    public void run() {
        OreProcessor.getInstance().playerDataManager.streamData((uuid, playerData) -> {
            for (String oreId : playerData.listOreIds()) {
                Ore ore = OreProcessor.getApi().getOre(oreId);
                if (ore == null) continue;

                OreTransform oreTransform = ore.getBestTransform(uuid);
                OreData oreData = playerData.getOreData(oreId);
                if (oreData == null) continue;

                Map<UMaterial, Integer> summary = oreData.process(1, oreTransform::convert);
                if (summary.isEmpty()) continue;
                int processed = summary.values().stream().reduce(0, Integer::sum);

                StatisticHelper.increaseProductCount(oreId, processed, playerData);
                StatisticHelper.increaseProductCount(oreId, processed, OreProcessor.getApi().getServerData());
                OreProcessor.getInstance().debug(2, String.format(
                        "Processed x%d %s for %s using transform #%s",
                        processed, ore.getName(), uuid, oreTransform.getId()
                ));
            }
        });
    }
}
