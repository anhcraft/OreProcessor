package dev.anhcraft.oreprocessor.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CraftingRecipe {
    private final ItemStack input;
    private final ItemStack output;

    public CraftingRecipe(@NotNull ItemStack input, @NotNull ItemStack output) {
        this.input = input;
        this.output = output;
    }

    @NotNull
    public ItemStack getInput() {
        return input;
    }

    @NotNull
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return String.format("%d %s > %d %s", input.getAmount(), input.getType(), output.getAmount(), output.getType());
    }
}
