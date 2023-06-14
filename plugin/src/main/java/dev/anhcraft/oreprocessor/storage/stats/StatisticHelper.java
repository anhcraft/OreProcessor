package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.oreprocessor.api.data.stats.TrackedData;

public class StatisticHelper {
    public static void increaseMiningCount(String ore, TrackedData trackedData) {
        trackedData.getCumulativeStats().addMiningCount(ore, 1);
        trackedData.getHourlyStats(System.currentTimeMillis()).addMiningCount(ore, 1);
    }
}
