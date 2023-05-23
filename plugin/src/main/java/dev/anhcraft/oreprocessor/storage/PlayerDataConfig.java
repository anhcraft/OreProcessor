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

    public int dataVersion;

    public boolean hideTutorial;

    public long hibernationStart;

    @Nullable
    public LinkedHashMap<String, OreDataConfig> ores;

    public void markDirty() {
        dirty.set(true);
    }
}
