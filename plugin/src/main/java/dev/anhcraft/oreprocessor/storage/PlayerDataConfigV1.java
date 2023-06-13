package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.oreprocessor.storage.compat.GenericPlayerDataConfig;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
class PlayerDataConfigV1 extends GenericPlayerDataConfig {
    @Exclude
    public AtomicBoolean dirty = new AtomicBoolean(false);

    public boolean hideTutorial;

    public long hibernationStart;

    @Nullable
    public LinkedHashMap<String, OreDataConfig> ores;

    public void markDirty() {
        dirty.set(true);
    }

    @PostHandler
    private void handle() {
        dirty = new AtomicBoolean(false); // sometimes this disappears
    }
}
