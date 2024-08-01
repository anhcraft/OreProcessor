package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class VanillaBridge implements ItemCustomizer {
    @Override
    public MaterialClass getMaterialClass() {
        return MaterialClass.VANILLA;
    }

    @Override
    public Set<UMaterial> getCustomMaterials() {
        return Set.of();
    }

    @Override
    public @Nullable ItemStack buildItem(@NotNull UMaterial material) {
        return new ItemStack(material.asBukkit());
    }

    @Override
    public @Nullable UItemStack identifyItem(@NotNull ItemStack item) {
        return UItemStack.of(item);
    }

    @Override
    public @Nullable UMaterial identifyMaterial(@NotNull ItemStack item) {
        return UMaterial.of(item.getType());
    }
}
