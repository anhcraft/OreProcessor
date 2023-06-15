package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.oreprocessor.api.data.stats.Statistics;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatisticsImpl implements Statistics {
    private final AtomicBoolean dirty;
    private final StatisticConfig config;

    public StatisticsImpl(AtomicBoolean dirty, StatisticConfig config) {
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

    @Override
    public long getMiningCount(String ore) {
        if (config.miningCount == null) return 0;
        return config.miningCount.getOrDefault(ore, 0L);
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
    public long getFeedstockCount(String ore) {
        if (config.feedstockCount == null) return 0;
        return config.feedstockCount.getOrDefault(ore, 0L);
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
    public long getProductCount(String ore) {
        if (config.productCount == null) return 0;
        return config.productCount.getOrDefault(ore, 0L);
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
