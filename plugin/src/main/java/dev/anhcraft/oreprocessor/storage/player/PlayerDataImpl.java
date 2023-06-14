package dev.anhcraft.oreprocessor.storage.player;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.data.stats.Statistics;
import dev.anhcraft.oreprocessor.storage.stats.StatisticsImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class PlayerDataImpl implements PlayerData {
    private final PlayerDataConfigV1 config;

    public PlayerDataImpl(@NotNull PlayerDataConfigV1 config) {
        this.config = config;
    }

    @Override
    public int getDataVersion() {
        return config.dataVersion;
    }

    @Override
    public boolean isTutorialHidden() {
        return config.hideTutorial;
    }

    @Override
    public void hideTutorial(boolean value) {
        config.hideTutorial = value;
        config.markDirty();
    }

    @Override
    public @NotNull List<String> listOreIds() {
        return config.ores == null ? Collections.emptyList() : new ArrayList<>(config.ores.keySet());
    }

    @Override
    public @Nullable OreData getOreData(@NotNull String ore) {
        if (config.ores == null) return null;
        return config.ores.containsKey(ore) ? new OreDataImpl(config.ores.get(ore), config.dirty) : null;
    }

    @Override
    public @NotNull OreData requireOreData(@NotNull String ore) {
        if (OreProcessor.getApi().getOre(ore) == null) // TODO: inform player name and UUID
            OreProcessor.getInstance().getLogger().warning(String.format("Attempting to require ore '%s' which does not exist in config", ore));

        if (config.ores == null)
            config.ores = new LinkedHashMap<>(); // marking dirty is redundant
        else if (config.ores.containsKey(ore))
            return new OreDataImpl(config.ores.get(ore), config.dirty);

        OreDataConfig oreDataConfig = new OreDataConfig();
        config.ores.put(ore, oreDataConfig); // marking dirty is redundant
        return new OreDataImpl(oreDataConfig, config.dirty);
    }

    @Override
    public long getHibernationStart() {
        return config.hibernationStart;
    }

    @Override
    public void setHibernationStart(long hibernationStart) {
        config.hibernationStart = hibernationStart;
        config.markDirty();
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }

    @Override
    public @NotNull Statistics getCumulativeStats() {
        return new StatisticsImpl(config.dirty, config.getStats().getCumulativeStats());
    }

    @Override
    public @NotNull Statistics getHourlyStats(long timestamp) {
        return new StatisticsImpl(config.dirty, config.getStats().getHourlyStat(timestamp));
    }
}
