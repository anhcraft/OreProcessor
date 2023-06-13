package dev.anhcraft.oreprocessor.api;

import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import dev.anhcraft.oreprocessor.api.upgrade.UpgradeLevel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OreProcessorApi {
    @NotNull
    List<String> getOres();

    @Nullable
    Ore getOre(@NotNull String id);

    @NotNull
    default Ore requireOre(@NotNull String id) {
        return Objects.requireNonNull(getOre(id), "Ore not found: " + id);
    }

    int getProcessingSpeed();

    int getDefaultCapacity();

    int getDefaultThroughput();

    int getThroughputPerMinute(int throughput);

    @NotNull
    IPlayerData getPlayerData(@NotNull Player player);

    @NotNull
    Optional<IPlayerData> getPlayerData(@NotNull UUID id);

    @NotNull
    CompletableFuture<IPlayerData> requirePlayerData(@NotNull UUID id);

    @Nullable
    UpgradeLevel getNextThroughputUpgrade(int currentThroughput);

    @Nullable
    UpgradeLevel getNextCapacityUpgrade(int currentCapacity);

    ShopProviderType getShopProvider();
}
