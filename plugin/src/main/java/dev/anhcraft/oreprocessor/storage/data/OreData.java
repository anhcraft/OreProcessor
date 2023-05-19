package dev.anhcraft.oreprocessor.storage.data;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class OreData implements IOreData {
    private final OreDataConfig config;

    public OreData(OreDataConfig config) {
        this.config = config;
    }

    @Override
    public int getThroughput() {
        synchronized (config) {
            return config.throughput <= 0 ? OreProcessor.getInstance().getDefaultThroughput() : config.throughput;
        }
    }

    @Override
    public int getCapacity() {
        synchronized (config) {
            return config.capacity <= 0 ? OreProcessor.getInstance().getDefaultCapacity() : config.capacity;
        }
    }

    @Override
    public @NotNull Set<Material> getFeedstock() {
        synchronized (config) {
            return config.feedstock == null ? Collections.emptySet() : Collections.unmodifiableSet(config.feedstock.keySet());
        }
    }

    @Override
    public @NotNull Set<Material> getProducts() {
        synchronized (config) {
            return config.products == null ? Collections.emptySet() : Collections.unmodifiableSet(config.products.keySet());
        }
    }

    @Override
    public int countFeedstock(@NotNull Material material) {
        synchronized (config) {
            return config.feedstock == null ? 0 : config.feedstock.getOrDefault(material, 0);
        }
    }

    @Override
    public int countAllFeedstock() {
        synchronized (config) {
            if (config.feedstock == null) return 0;
            int sum = 0;
            for (int value : config.feedstock.values()) {
                sum += value;
            }
            return sum;
        }
    }

    @Override
    public int countProduct(@NotNull Material material) {
        synchronized (config) {
            return config.products == null ? 0 : config.products.getOrDefault(material, 0);
        }
    }

    @Override
    public int countAllProducts() {
        synchronized (config) {
            if (config.products == null) return 0;
            int sum = 0;
            for (int value : config.products.values()) {
                sum += value;
            }
            return sum;
        }
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }
}
