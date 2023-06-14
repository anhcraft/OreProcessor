package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.oreprocessor.api.data.stats.Monitored;

public class StatisticHelper {
    public static void increaseMiningCount(String ore, Monitored trackedData) {
        trackedData.getCumulativeStats().addMiningCount(ore, 1);
        trackedData.getHourlyStats(System.currentTimeMillis()).addMiningCount(ore, 1);
    }

    public static void increaseFeedstockCount(String ore, int amount, Monitored trackedData) {
        trackedData.getCumulativeStats().addFeedstockCount(ore, amount);
        trackedData.getHourlyStats(System.currentTimeMillis()).addFeedstockCount(ore, amount);
    }

    public static void increaseProductCount(String ore, int amount, Monitored trackedData) {
        trackedData.getCumulativeStats().addProductCount(ore, amount);
        trackedData.getHourlyStats(System.currentTimeMillis()).addProductCount(ore, amount);
    }
}
