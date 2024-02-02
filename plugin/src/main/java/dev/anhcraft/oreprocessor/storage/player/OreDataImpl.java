package dev.anhcraft.oreprocessor.storage.player;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class OreDataImpl implements OreData {
    private final OreDataConfig config;
    private final AtomicBoolean dirty;

    public OreDataImpl(@NotNull OreDataConfig config, @NotNull AtomicBoolean dirty) {
        this.config = config;
        this.dirty = dirty;
    }

    @Override
    public int getThroughput() {
        synchronized (config) {
            return config.throughput <= 0 ? OreProcessor.getApi().getDefaultThroughput() : config.throughput;
        }
    }

    @Override
    public void setThroughput(int amount) {
        synchronized (config) {
            if (amount < 0 || config.throughput == amount) return;
            config.throughput = amount;
            markDirty();
        }
    }

    @Override
    public void addThroughput(int amount) {
        synchronized (config) {
            config.throughput = Math.max(0, config.throughput + amount);
            markDirty();
        }
    }

    @Override
    public int getCapacity() {
        synchronized (config) {
            return config.capacity <= 0 ? OreProcessor.getApi().getDefaultCapacity() : config.capacity;
        }
    }

    @Override
    public void setCapacity(int amount) {
        synchronized (config) {
            if (amount < 0 || config.capacity == amount) return;
            config.capacity = amount;
            markDirty();
        }
    }

    @Override
    public void addCapacity(int amount) {
        synchronized (config) {
            config.capacity = Math.max(0, config.capacity + amount);
            markDirty();
        }
    }

    @Override
    public @NotNull Set<UMaterial> getFeedstock() {
        synchronized (config) {
            return config.feedstock == null ? Collections.emptySet() : new HashSet<>(config.feedstock.keySet()); // clone to prevent CME
        }
    }

    @Override
    public void addFeedstock(@NotNull UMaterial material, int amount) {
        synchronized (config) {
            int newVal = amount;
            if (config.feedstock == null)
                config.feedstock = new LinkedHashMap<>();
            else
                newVal += config.feedstock.getOrDefault(material, 0);
            newVal = Math.max(newVal, 0);

            if (!Objects.equals(config.feedstock.put(material, newVal), newVal)) {
                markDirty();
            }
        }
    }

    @Override
    public int countFeedstock(@NotNull UMaterial material) {
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
    public @NotNull Set<UMaterial> getProducts() {
        synchronized (config) {
            return config.products == null ? Collections.emptySet() : new HashSet<>(config.products.keySet()); // clone to prevent CME
        }
    }

    @Override
    public int addProduct(@NotNull UMaterial material, int expectedAmount, boolean force) {
        synchronized (config) {
            int toStore = force ? expectedAmount : Math.min(expectedAmount, getCapacity() - countAllProducts());
            int newVal = countProduct(material) + toStore;
            newVal = Math.max(newVal, 0);

            if (config.products == null)
                config.products = new LinkedHashMap<>();

            if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                markDirty();
            }

            return toStore;
        }
    }

    @Override
    public int takeProduct(@NotNull UMaterial material, int expectedAmount) {
        synchronized (config) {
            int stored = countProduct(material);
            int toTake = Math.min(expectedAmount, stored);
            int newVal = Math.max(stored - toTake, 0);

            if (config.products == null) {
                toTake = 0;
            } else if (newVal == 0) {
                config.products.remove(material);
                markDirty();
            } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                markDirty();
            }

            return toTake;
        }
    }

    @Override
    public int setProduct(@NotNull UMaterial material, int expectedAmount, boolean force) {
        synchronized (config) {
            int toStore = force ? expectedAmount : Math.min(Math.max(expectedAmount, 0), getCapacity() - countAllProducts() + countProduct(material));

            if (config.products == null)
                config.products = new LinkedHashMap<>();

            if (!Objects.equals(config.products.put(material, toStore), toStore)) {
                markDirty();
            }

            return toStore;
        }
    }

    @Override
    public boolean testAndTakeProduct(@NotNull UMaterial material, int expectedAmount, @NotNull Function<Integer, Boolean> function) {
        synchronized (config) {
            int stored = countProduct(material);
            int toTake = Math.min(expectedAmount, stored);

            if (config.products != null && toTake > 0 && function.apply(toTake)) {
                int newVal = Math.max(stored - toTake, 0);

                if (newVal == 0) {
                    config.products.remove(material);
                    markDirty();
                } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                    markDirty();
                }

                return true;
            }

            return false;
        }
    }

    @Override
    public boolean testAndSetProduct(@NotNull UMaterial material, @NotNull Function<Integer, Integer> function) {
        synchronized (config) {
            int newVal;

            if (config.products != null && (newVal = function.apply(countProduct(material))) >= 0) {

                if (newVal == 0) {
                    config.products.remove(material);
                    markDirty();
                } else if (!Objects.equals(config.products.put(material, newVal), newVal)) {
                    markDirty();
                }

                return true;
            }

            return false;
        }
    }

    @Override
    public int countProduct(@NotNull UMaterial material) {
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
    public boolean isFull() {
        synchronized (config) {
            return countAllFeedstock() + countAllProducts() >= getCapacity();
        }
    }

    @Override
    public int getFreeSpace() {
        synchronized (config) {
            return Math.max(0, getCapacity() - (countAllFeedstock() + countAllProducts()));
        }
    }

    @Override
    public Map<UMaterial, Integer> process(int throughputMultiplier, @NotNull Function<UMaterial, UItemStack> function) {
        Map<UMaterial, Integer> summary = new HashMap<>();

        synchronized (config) {
            if (config.feedstock == null)
                return Collections.emptyMap();

            int totalStored = countAllProducts();
            int capacity = getCapacity();

            for (Iterator<Map.Entry<UMaterial, Integer>> it = config.feedstock.entrySet().iterator(); it.hasNext() && totalStored < capacity; ) {
                Map.Entry<UMaterial, Integer> en = it.next();
                UItemStack output = function.apply(en.getKey());
                if (output.isEmpty() || !output.getMaterial().isItem())
                    continue;
                UMaterial product = output.getMaterial();

                int availableStorage = capacity - totalStored;
                int actualQueued = Math.min(en.getValue(), getThroughput() * throughputMultiplier);
                // ensure that the output multiplication will not make storage overfull
                actualQueued = Math.min(actualQueued * output.getAmount(), availableStorage) / output.getAmount();

                if (actualQueued == 0)
                    continue;

                int createdProduct = actualQueued * output.getAmount();

                if (config.products == null)
                    config.products = new LinkedHashMap<>();
                config.products.put(product, config.products.getOrDefault(product, 0) + createdProduct);
                summary.put(product, summary.getOrDefault(product, 0) + createdProduct);
                totalStored += createdProduct;

                int newQueued = en.getValue() - actualQueued;
                if (newQueued == 0)
                    it.remove(); // If nothing left, remove from feedstock
                else
                    en.setValue(newQueued); // If still exists, let do it in the next processing time

                markDirty();
            }

            return summary;
        }
    }

    private void markDirty() {
        dirty.set(true);
    }

    @Override
    public boolean isDirty() {
        return dirty.get();
    }
}
