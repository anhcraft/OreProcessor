package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
class PlayerDataConfig {
    @Exclude
    public final AtomicBoolean dirty = new AtomicBoolean(false);

    public int dataVersion = 1; // TODO Change this on new data version

    public boolean hideTutorial;

    public long hibernationStart;

    @Nullable
    public LinkedHashMap<String, OreDataConfig> ores;

    public void markDirty() {
        dirty.set(true);
    }
}
