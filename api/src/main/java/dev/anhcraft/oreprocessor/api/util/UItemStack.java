package dev.anhcraft.oreprocessor.api.util;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract wrapper for {@link ItemStack} to work with different materials from vanilla and custom item plugins.
 */
public record UItemStack(@NotNull UMaterial material, int amount) {
    public static UItemStack of(@NotNull ItemStack itemStack) {
        return new UItemStack(UMaterial.of(itemStack.getType()), itemStack.getAmount());
    }

    public UItemStack(@NotNull UMaterial material, int amount) {
        this.material = material;
        this.amount = amount;
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
    public String toString() {
        return "UItemStack{" +
                "material=" + material +
                ", amount=" + amount +
                '}';
    }
}
