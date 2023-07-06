package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.oreprocessor.api.data.stats.Monitored;

public class StatisticHelper {
    public static void increaseMiningCount(String ore, Monitored trackedData) {
        trackedData.getCumulativeStats().addMiningCount(ore, 1);
        trackedData.getOrCreateHourlyStats(System.currentTimeMillis()).addMiningCount(ore, 1);
    }

    public static void increaseFeedstockCount(String ore, int amount, Monitored trackedData) {
        trackedData.getCumulativeStats().addFeedstockCount(ore, amount);
        trackedData.getOrCreateHourlyStats(System.currentTimeMillis()).addFeedstockCount(ore, amount);
    }

    public static void increaseProductCount(String ore, int amount, Monitored trackedData) {
        trackedData.getCumulativeStats().addProductCount(ore, amount);
        trackedData.getOrCreateHourlyStats(System.currentTimeMillis()).addProductCount(ore, amount);
    }
}
