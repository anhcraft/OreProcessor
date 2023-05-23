package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreProcessorApi;
import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import dev.anhcraft.oreprocessor.storage.PlayerData;
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
    public @NotNull IPlayerData getPlayerData(@NotNull Player player) {
        return new PlayerData(plugin.playerDataManager.getData(player));
    }

    @Override
    public @NotNull Optional<IPlayerData> getPlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.getData(id).map(PlayerData::new);
    }

    @Override
    public @NotNull CompletableFuture<IPlayerData> requirePlayerData(@NotNull UUID id) {
        return plugin.playerDataManager.requireData(id);
    }
}
