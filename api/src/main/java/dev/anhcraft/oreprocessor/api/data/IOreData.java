package dev.anhcraft.oreprocessor.api.data;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface IOreData extends ModifiableData {
    int getThroughput();

    int getCapacity();

    @NotNull
    Set<Material> getFeedstock();

    @NotNull
    Set<Material> getProducts();

    int countFeedstock(@NotNull Material material);

    int countAllFeedstock();

    int countProduct(@NotNull Material material);

    int countAllProducts();

    default boolean isFull() {
        return countAllFeedstock() + countAllProducts() >= getCapacity();
    }
}
