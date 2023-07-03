package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.oreprocessor.api.data.stats.Statistics;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatisticsImpl implements Statistics {
    private final AtomicBoolean dirty;
    private final StatisticConfig config;

    public StatisticsImpl(AtomicBoolean dirty, @NotNull StatisticConfig config) {
        this.dirty = dirty;
        this.config = config;
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }

    public void markDirty() {
        dirty.set(true);
    }

    private long getCount(Map<String, Long> map, String oreQuery) {
        oreQuery = oreQuery.trim();
        if (oreQuery.equals("*")) {
            long total = 0;
            for (long n : map.values()) total += n;
            return total;
        } else {
            String[] ores = oreQuery.split(",");
            long total = 0;
            for (String ore : ores)
                total += map.getOrDefault(ore, 0L);
            return total;
        }
    }

    @Override
    public synchronized long getMiningCount(String oreQuery) {
        return getCount(config.miningCount, oreQuery);
    }

    @Override
    public synchronized void setMiningCount(String ore, long amount) {
        if (config.miningCount == null)
            config.miningCount = new HashMap<>();
        config.miningCount.put(ore, amount);
        markDirty();
    }

    @Override
    public void addMiningCount(String ore, long amount) {
        setMiningCount(ore, getMiningCount(ore) + amount);
    }

    @Override
    public synchronized long getFeedstockCount(String oreQuery) {
        return getCount(config.feedstockCount, oreQuery);
    }

    @Override
    public synchronized void setFeedstockCount(String ore, long amount) {
        if (config.feedstockCount == null)
            config.feedstockCount = new HashMap<>();
        config.feedstockCount.put(ore, amount);
        markDirty();
    }

    @Override
    public void addFeedstockCount(String ore, long amount) {
        setFeedstockCount(ore, getFeedstockCount(ore) + amount);
    }

    @Override
    public synchronized long getProductCount(String oreQuery) {
        return getCount(config.productCount, oreQuery);
    }

    @Override
    public synchronized void setProductCount(String ore, long amount) {
        if (config.productCount == null)
            config.productCount = new HashMap<>();
        config.productCount.put(ore, amount);
        markDirty();
    }

    @Override
    public void addProductCount(String ore, long amount) {
        setProductCount(ore, getProductCount(ore) + amount);
    }
}
