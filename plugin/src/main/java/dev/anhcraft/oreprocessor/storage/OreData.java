package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class OreData implements IOreData {
    private final OreDataConfig config;

    public OreData(@NotNull OreDataConfig config) {
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
            if (config.throughput == amount) return;
            config.throughput = amount;
            config.markDirty();
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
            if (config.capacity == amount) return;
            config.capacity = amount;
            config.markDirty();
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
            int newVal = amount;
            if (config.feedstock == null)
                config.feedstock = new LinkedHashMap<>();
            else
                newVal += config.feedstock.getOrDefault(material, 0);

            if (!Objects.equals(config.feedstock.put(material, newVal), newVal)) {
                config.markDirty();
            }
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
            int totalStored = countAllProducts();
            int cap = getCapacity();
            int toStore = force ? expectedAmount : Math.min(expectedAmount, cap - totalStored);
            int newVal = countProduct(material) + toStore;

            if (config.products == null)
                config.products = new LinkedHashMap<>();

            if (!Objects.equals(config.products.put(material, newVal), newVal)) {
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
            } else if (newVal == 0) {
                config.products.remove(material);
                config.markDirty();
            } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                config.markDirty();
            }

            return expectedAmount - toTake;
        }
    }

    @Override
    public boolean testAndTakeProduct(@NotNull Material material, int expectedAmount, @NotNull Function<Integer, Boolean> function) {
        synchronized (config) {
            int stored = countProduct(material);
            int toTake = Math.min(expectedAmount, stored);

            if (config.products != null && toTake > 0 && function.apply(toTake)) {
                int newVal = stored - toTake;

                if (newVal == 0) {
                    config.products.remove(material);
                    config.markDirty();
                } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
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
    public void process(int throughputMultiplier, @NotNull UnaryOperator<Material> function) {
        synchronized (config) {
            if (config.feedstock == null) return;

            int totalStored = countAllProducts();
            int cap = getCapacity();

            for (Iterator<Map.Entry<Material, Integer>> it = config.feedstock.entrySet().iterator(); it.hasNext() && totalStored < cap; ) {
                Map.Entry<Material, Integer> en = it.next();
                Material source = en.getKey();
                Material output = function.apply(source);
                int queued = en.getValue();

                int toStore = Math.min(queued, getThroughput() * throughputMultiplier);
                toStore = Math.min(toStore, cap - totalStored);
                if (toStore == 0) break;

                if (config.products == null)
                    config.products = new LinkedHashMap<>();

                config.products.put(output, config.products.getOrDefault(output, 0) + toStore);
                totalStored += toStore;

                int newQueued = queued - toStore;

                if (newQueued == 0)
                    it.remove();
                else
                    en.setValue(newQueued);

                config.markDirty();
            }
        }
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }
}
