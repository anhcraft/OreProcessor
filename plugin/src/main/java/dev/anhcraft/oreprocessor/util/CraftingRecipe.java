package dev.anhcraft.oreprocessor.util;

import dev.anhcraft.oreprocessor.api.util.UItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftingRecipe {
    private final UItemStack input;
    private final UItemStack output;

    public CraftingRecipe(@NotNull UItemStack input, @NotNull UItemStack output) {
        this.input = input;
        this.output = output;
    }

    @NotNull
    public UItemStack getInput() {
        return input;
    }

    @NotNull
    public UItemStack getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return String.format("%d %s > %d %s", input.amount(), input.material(), output.amount(), output.material());
    }
}
