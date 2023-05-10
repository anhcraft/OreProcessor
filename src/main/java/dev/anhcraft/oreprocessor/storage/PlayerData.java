package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.oreprocessor.OreProcessor;
import org.bukkit.Material;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
public class PlayerData {
    @Exclude
    public final AtomicBoolean dirty = new AtomicBoolean(false);

    @Validation(notNull = true, silent = true)
    private Map<Material, Integer> queuedOre; // product; amount

    @Validation(notNull = true, silent = true)
    private Map<Material, Integer> storage; // product, amount

    @Validation(notNull = true, silent = true)
    private Map<Material, Integer> throughput; // product, amount

    @Validation(notNull = true, silent = true)
    private Map<Material, Integer> capacity; // product, amount

    public void markDirty() {
        dirty.set(true);
    }

    public int countQueuedOre(Material product) {
        return queuedOre.getOrDefault(product, 0);
    }

    public int countStorage(Material product) {
        return storage.getOrDefault(product, 0);
    }

    public int getThroughput(Material product) {
        return throughput.getOrDefault(product, OreProcessor.getInstance().mainConfig.throughputUpgrade.get("default").amount);
    }

    public int getCapacity(Material product) {
        return capacity.getOrDefault(product, OreProcessor.getInstance().mainConfig.capacityUpgrade.get("default").amount);
    }

    public void queueOre(Material product, int amount) {
        synchronized (dirty) {
            queuedOre.put(product, queuedOre.getOrDefault(product, 0) + amount);
        }
    }

    public void processOre(Material ore) {
        synchronized (dirty) {
            int queued = queuedOre.getOrDefault(ore, 0);
            int stored = storage.getOrDefault(ore, 0);
            int cap = capacity.getOrDefault(ore, 0);
            int toStore = Math.min(queued, throughput.get(ore));
            toStore = Math.min(toStore, cap - stored);
            storage.put(ore, stored + toStore);
            queuedOre.put(ore, queued - toStore);
        }
    }

    public int storeOre(Material ore, int amount) {
        synchronized (dirty) {
            int stored = storage.getOrDefault(ore, 0);
            int cap = capacity.getOrDefault(ore, 0);
            int toStore = Math.min(amount, cap - stored);
            storage.put(ore, stored + toStore);
            return toStore;
        }
    }

    public int takeOre(Material ore, int amount) {
        synchronized (dirty) {
            int stored = storage.getOrDefault(ore, 0);
            int toTake = Math.max(0, stored - amount);
            storage.put(ore, stored - toTake);
            return toTake;
        }
    }

    public void setThroughput(Material ore, int amount) {
        synchronized (dirty) {
            throughput.put(ore, amount);
        }
    }

    public void setCapacity(Material ore, int amount) {
        synchronized (dirty) {
            capacity.put(ore, amount);
        }
    }
}
