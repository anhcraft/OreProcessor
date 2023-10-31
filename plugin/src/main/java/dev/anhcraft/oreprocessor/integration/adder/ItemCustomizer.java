package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface ItemCustomizer {
    MaterialClass getMaterialClass();

    Set<String> getCustomMaterials();

    ItemStack buildItem(UMaterial material);

    @Nullable
    UItemStack identifyItem(ItemStack item);

    @Nullable
    UMaterial identifyMaterial(ItemStack item);
}
