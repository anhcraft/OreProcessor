package dev.anhcraft.oreprocessor.storage.data;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.oreprocessor.OreProcessor;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@Configurable
class PlayerDataConfig {
    @Exclude
    public final AtomicBoolean dirty = new AtomicBoolean(false);

    public boolean hideTutorial;

    public long hibernationStart;

    @Validation(notNull = true, silent = true)
    public LinkedHashMap<String, OreDataConfig> ores = new LinkedHashMap<>();

    public void markDirty() {
        dirty.set(true);
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
