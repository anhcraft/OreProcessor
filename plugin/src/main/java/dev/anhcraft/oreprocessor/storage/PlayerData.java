package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

public class PlayerData implements IPlayerData {
    private final PlayerDataConfig config;

    public PlayerData(@NotNull PlayerDataConfig config) {
        this.config = config;
    }

    @Override
    public int getDataVersion() {
        return config.dataVersion;
    }

    @Override
    public boolean hasHideTutorial() {
        return config.hideTutorial;
    }

    @Override
    public void setHideTutorial(boolean value) {
        config.hideTutorial = value;
        config.markDirty();
    }

    @Override
    public @NotNull List<String> listOreIds() {
        return config.ores == null ? Collections.emptyList() : new ArrayList<>(config.ores.keySet());
    }

    @Override
    public @Nullable IOreData getOreData(@NotNull String ore) {
        if (config.ores == null) return null;
        return config.ores.containsKey(ore) ? new OreData(config.ores.get(ore), config.dirty) : null;
    }

    @Override
    public @NotNull IOreData requireOreData(@NotNull String ore) {
        if (OreProcessor.getApi().getOre(ore) == null) // TODO: inform player name and UUID
            OreProcessor.getInstance().getLogger().warning(String.format("Attempting to require ore '%s' which does not exist in config", ore));

        if (config.ores == null)
            config.ores = new LinkedHashMap<>(); // marking dirty is redundant
        else if (config.ores.containsKey(ore))
            return new OreData(config.ores.get(ore), config.dirty);

        OreDataConfig oreDataConfig = new OreDataConfig();
        config.ores.put(ore, oreDataConfig); // marking dirty is redundant
        return new OreData(oreDataConfig, config.dirty);
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
}
