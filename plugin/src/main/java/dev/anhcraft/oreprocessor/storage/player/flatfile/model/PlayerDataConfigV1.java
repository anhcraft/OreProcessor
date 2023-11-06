package dev.anhcraft.oreprocessor.storage.player.flatfile.model;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Exclude;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.oreprocessor.storage.stats.StatisticTimeSeries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
public class PlayerDataConfigV1 extends GenericPlayerDataConfig {
    @Exclude
    public AtomicBoolean dirty = new AtomicBoolean(false);

    public boolean hideTutorial;

    public long hibernationStart;

    @Nullable
    public LinkedHashMap<String, OreDataConfig> ores;

    private StatisticTimeSeries stats;

    public void markDirty() {
        dirty.set(true);
    }

    @NotNull
    public StatisticTimeSeries getStats() {
        if (stats == null) stats = new StatisticTimeSeries();
        return stats;
    }

    @PostHandler
    private void handle() {
        dirty = new AtomicBoolean(false); // sometimes this disappears
    }
}
