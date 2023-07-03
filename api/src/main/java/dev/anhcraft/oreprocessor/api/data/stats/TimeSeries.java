package dev.anhcraft.oreprocessor.api.data.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimeSeries {
    private long miningCount;
    private long feedstockCount;
    private long productCount;

    private TimeSeries(long miningCount, long feedstockCount, long productCount) {
        this.miningCount = miningCount;
        this.feedstockCount = feedstockCount;
        this.productCount = productCount;
    }

    public long getMiningCount() {
        return miningCount;
    }

    public long getFeedstockCount() {
        return feedstockCount;
    }

    public long getProductCount() {
        return productCount;
    }

    @NotNull
    public static TimeSeries parseAll(@NotNull Monitored monitored, @Nullable String ore) {
        return new TimeSeries(
            monitored.getCumulativeStats().getMiningCount(ore),
            monitored.getCumulativeStats().getFeedstockCount(ore),
            monitored.getCumulativeStats().getProductCount(ore)
        );
    }

    @NotNull
    public static TimeSeries parseRange(@NotNull Monitored monitored, @Nullable String ore, long fromDate, long endDate) {
        if (fromDate <= 0) fromDate = System.currentTimeMillis();
        if (endDate <= 0) endDate = System.currentTimeMillis();
        long miningCount = 0;
        long feedstockCount = 0;
        long productCount = 0;
        for (long i = fromDate; i <= endDate; i += 3600000L) {
            Statistics stats = monitored.getHourlyStats(i);
            if (stats == null) continue;
            miningCount += stats.getMiningCount(ore);
            feedstockCount += stats.getFeedstockCount(ore);
            productCount += stats.getProductCount(ore);
        }
        return new TimeSeries(miningCount, feedstockCount, productCount);
    }
}
