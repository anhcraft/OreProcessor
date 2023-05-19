package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreProcessorApi;
import dev.anhcraft.oreprocessor.storage.data.PlayerDataConfig;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class OreProcessorApiImpl implements OreProcessorApi {
    private final OreProcessor plugin;
    private final Map<String, Ore> ores = new HashMap<>();

    public OreProcessorApiImpl(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull List<String> getOres() {
        return new ArrayList<>(ores.keySet());
    }

    @Override
    public @Nullable Ore getOre(@NotNull String id) {
        return ores.get(id);
    }

    @Override
    public @NotNull PlayerDataConfig getPlayerData(@NotNull Player player) {
        return plugin.playerDataManager.getData(player);
    }

    @Override
    public @NotNull Optional<PlayerDataConfig> getPlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.getData(id);
    }

    @Override
    public @NotNull CompletableFuture<PlayerDataConfig> requirePlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.requireData(id);
    }
}
