package dev.anhcraft.oreprocessor.storage.data;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

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
    public void setThroughput(int amount) {
        synchronized (config) {
            config.throughput = amount;
        }
    }

    @Override
    public int getCapacity() {
        synchronized (config) {
            return config.capacity <= 0 ? OreProcessor.getInstance().getDefaultCapacity() : config.capacity;
        }
    }

    @Override
    public void setCapacity(int amount) {
        synchronized (config) {
            config.capacity = amount;
        }
    }

    @Override
    public @NotNull Set<Material> getFeedstock() {
        synchronized (config) {
            return config.feedstock == null ? Collections.emptySet() : Collections.unmodifiableSet(config.feedstock.keySet());
        }
    }

    @Override
    public void addFeedstock(@NotNull Material material, int amount) {
        synchronized (config) {
            if (config.feedstock == null) {
                config.feedstock = new LinkedHashMap<>();
            }
            config.feedstock.put(material, amount);
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
    public @NotNull Set<Material> getProducts() {
        synchronized (config) {
            return config.products == null ? Collections.emptySet() : Collections.unmodifiableSet(config.products.keySet());
        }
    }

    @Override
    public int addProduct(@NotNull Material material, int expectedAmount, boolean force) {
        synchronized (config) {
            int stored = countProduct(material);
            int cap = getCapacity();
            int toStore = force ? expectedAmount : Math.min(expectedAmount, cap - stored);
            int newVal = stored + toStore;

            if (config.products == null) {
                config.products = new LinkedHashMap<>();
                config.products.put(material, toStore);
                config.markDirty();
            } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                config.markDirty();
            }

            return expectedAmount - toStore;
        }
    }

    @Override
    public int takeProduct(@NotNull Material material, int expectedAmount) {
        synchronized (config) {
            int stored = countProduct(material);
            int toTake = Math.min(expectedAmount, stored);
            int newVal = stored - toTake;

            if (config.products == null) {
                toTake = 0;
            } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                config.markDirty();
            }

            return expectedAmount - toTake;
        }
    }

    @Override
    public boolean testAndTakeProduct(@NotNull Material material, int expectedAmount, Function<Integer, Boolean> function) {
        synchronized (config) {
            int stored = countProduct(material);
            int toTake = Math.min(expectedAmount, stored);

            if (config.products != null && toTake > 0 && function.apply(toTake)) {
                int newVal = stored - toTake;
                if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                    config.markDirty();
                }
                return true;
            }

            return false;
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
