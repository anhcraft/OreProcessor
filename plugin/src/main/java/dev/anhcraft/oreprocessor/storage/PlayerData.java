package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.oreprocessor.api.data.IOreData;
import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

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
    public @NotNull List<IOreData> listOreData() {
        return config.ores == null ? Collections.emptyList() : config.ores.values().stream().map(OreData::new).collect(Collectors.toList());
    }

    @Override
    public @Nullable IOreData getOreData(@NotNull String ore) {
        if (config.ores == null) return null;
        return config.ores.containsKey(ore) ? new OreData(config.ores.get(ore)) : null;
    }

    @Override
    public @NotNull IOreData requireOreData(@NotNull String ore) {
        if (config.ores == null)
            config.ores = new LinkedHashMap<>(); // marking dirty is redundant
        if (config.ores.containsKey(ore))
            return new OreData(config.ores.get(ore));
        OreDataConfig oreDataConfig = new OreDataConfig();
        config.ores.put(ore, oreDataConfig); // marking dirty is redundant
        return new OreData(oreDataConfig);
    }

    @Override
    public boolean isDirty() {
        return config.dirty.get();
    }
}
