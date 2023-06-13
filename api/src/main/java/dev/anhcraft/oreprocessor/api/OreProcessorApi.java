package dev.anhcraft.oreprocessor.api;

import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import dev.anhcraft.oreprocessor.api.upgrade.UpgradeLevel;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public interface OreProcessorApi {
    @NotNull
    Set<String> getOres();

    @Nullable
    Ore getOre(@NotNull String id);

    @NotNull
    default Ore requireOre(@NotNull String id) {
        return Objects.requireNonNull(getOre(id), "Ore not found: " + id);
    }

    @Nullable
    Ore getBlockOre(Material block);

    @NotNull
    Collection<Ore> getOresAllowFeedstock(Material feedstock);

    int getProcessingSpeed();

    int getDefaultCapacity();

    int getDefaultThroughput();

    int getThroughputPerMinute(int throughput);

    @NotNull
    PlayerData getPlayerData(@NotNull Player player);

    @NotNull
    Optional<PlayerData> getPlayerData(@NotNull UUID id);

    @NotNull
    CompletableFuture<PlayerData> requirePlayerData(@NotNull UUID id);

    @Nullable
    UpgradeLevel getNextThroughputUpgrade(int currentThroughput);

    @Nullable
    UpgradeLevel getNextCapacityUpgrade(int currentCapacity);

    ShopProviderType getShopProvider();
}
