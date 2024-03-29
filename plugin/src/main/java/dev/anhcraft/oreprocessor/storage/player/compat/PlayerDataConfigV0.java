package dev.anhcraft.oreprocessor.storage.player.compat;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.oreprocessor.OreProcessor;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Configurable
public class PlayerDataConfigV0 extends GenericPlayerDataConfig {
    @Exclude
    public final AtomicBoolean dirty = new AtomicBoolean(false);

    public boolean hideTutorial;

    public long hibernationStart;

    @Validation(notNull = true, silent = true)
    private LinkedHashMap<Material, Integer> throughput = new LinkedHashMap<>(); // product, amount

    @Validation(notNull = true, silent = true)
    private LinkedHashMap<Material, Integer> queuedOre = new LinkedHashMap<>(); // product; amount

    @Validation(notNull = true, silent = true)
    private LinkedHashMap<Material, Integer> storage = new LinkedHashMap<>(); // product, amount

    @Validation(notNull = true, silent = true)
    private LinkedHashMap<Material, Integer> capacity = new LinkedHashMap<>(); // product, amount

    public void markDirty() {
        dirty.set(true);
    }

    public int getThroughput(Material product) {
        return throughput.getOrDefault(product, OreProcessor.getApi().getDefaultThroughput());
    }

    public int getCapacity(Material product) {
        return capacity.getOrDefault(product, OreProcessor.getApi().getDefaultCapacity());
    }

    public int countQueuedOre(Material product) {
        synchronized (dirty) {
            return queuedOre.getOrDefault(product, 0);
        }
    }

    public int countStorage(Material product) {
        synchronized (dirty) {
            return storage.getOrDefault(product, 0);
        }
    }

    public boolean isStorageFull(Material product) {
        synchronized (dirty) {
            return storage.getOrDefault(product, 0) + queuedOre.getOrDefault(product, 0) >= capacity.getOrDefault(product, OreProcessor.getApi().getDefaultCapacity());
        }
    }

    public void queueOre(Material product, int amount) {
        synchronized (dirty) {
            queuedOre.put(product, queuedOre.getOrDefault(product, 0) + amount);
            markDirty();
        }
    }

    public void processOre(int throughputMultiplier) {
        synchronized (dirty) {
            for (Map.Entry<Material, Integer> en : queuedOre.entrySet()) {
                Material product = en.getKey();
                int queued = en.getValue();
                int stored = storage.getOrDefault(product, 0);
                int cap = getCapacity(product);
                if (stored >= cap) continue;
                int toStore = Math.min(queued, getThroughput(product) * throughputMultiplier);
                toStore = Math.min(toStore, cap - stored);
                if (toStore == 0) continue;
                storage.put(product, stored + toStore);
                queuedOre.put(product, queued - toStore);
                markDirty();
            }
        }
    }

    public void processOre() {
        processOre(1);
    }

    public int storeOre(Material ore, int expectAmount) {
        synchronized (dirty) {
            int stored = storage.getOrDefault(ore, 0);
            int cap = getCapacity(ore);
            int toStore = Math.min(expectAmount, cap - stored);
            int newVal = stored + toStore;
            if (!Objects.equals(storage.put(ore, newVal), newVal)) {
                markDirty();
            }
            return toStore;
        }
    }

    public int takeOre(Material ore, int expectAmount) {
        synchronized (dirty) {
            int stored = storage.getOrDefault(ore, 0);
            int toTake = Math.min(stored, expectAmount);
            int newVal = stored - toTake;
            if (!Objects.equals(storage.put(ore, newVal), newVal)) {
                markDirty();
            }
            return toTake;
        }
    }

    public boolean testAndTakeOre(Material ore, int expectAmount, Function<Integer, Boolean> function) {
        synchronized (dirty) {
            int stored = storage.getOrDefault(ore, 0);
            int toTake = Math.min(stored, expectAmount);
            if (toTake > 0 && function.apply(toTake)) {
                int newVal = stored - toTake;
                if (!Objects.equals(storage.put(ore, newVal), newVal)) {
                    markDirty();
                }
                return true;
            }
            return false;
        }
    }

    public void setThroughput(Material ore, int amount) {
        synchronized (dirty) {
            throughput.put(ore, amount);
            markDirty();
        }
    }

    public void setCapacity(Material ore, int amount) {
        synchronized (dirty) {
            capacity.put(ore, amount);
            markDirty();
        }
    }
}
