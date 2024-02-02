package dev.anhcraft.oreprocessor.api.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract wrapper for {@link ItemStack} to work with different materials from vanilla and custom item plugins.
 */
public class UItemStack {
    private final UMaterial material;
    private final int amount;

    public static UItemStack of(@NotNull ItemStack itemStack) {
        return new UItemStack(UMaterial.of(itemStack.getType()), itemStack.getAmount());
    }

    public UItemStack(@NotNull UMaterial material, int amount) {
        this.material = material;
        this.amount = amount;
    }

    @NotNull
    public UMaterial getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isEmpty() {
        return amount <= 0 || material.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UItemStack)) return false;
        UItemStack that = (UItemStack) o;
        if (amount != that.amount) return false;
        return material.equals(that.material);
    }

    @Override
    public int hashCode() {
        int result = material.hashCode();
        result = 31 * result + amount;
        return result;
    }

    @Override
    public String toString() {
        return "UItemStack{" +
                "material=" + material +
                ", amount=" + amount +
                '}';
    }
}
