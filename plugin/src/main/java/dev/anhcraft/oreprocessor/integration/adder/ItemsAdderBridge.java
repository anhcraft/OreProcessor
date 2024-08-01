package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.integration.Integration;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class ItemsAdderBridge implements Integration, ItemCustomizer {
    private final OreProcessor plugin;

    public ItemsAdderBridge(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Override
    public MaterialClass getMaterialClass() {
        return MaterialClass.ITEMSADDER;
    }

    @Override
    public Set<UMaterial> getCustomMaterials() {
        return CustomStack.getNamespacedIdsInRegistry().stream().map(UMaterial::fromItemsAdder).collect(Collectors.toSet());
    }

    @Override
    public ItemStack buildItem(@NotNull UMaterial material) {
        CustomStack cs = CustomStack.getInstance(material.getIdentifier());
        return cs == null ? null : cs.getItemStack();
    }

    @Override
    public UItemStack identifyItem(@NotNull ItemStack item) {
        CustomStack cs = CustomStack.byItemStack(item);
        return cs == null ? null : new UItemStack(UMaterial.fromItemsAdder(cs.getNamespacedID()), item.getAmount());
    }

    @Override
    public UMaterial identifyMaterial(@NotNull ItemStack item) {
        CustomStack cs = CustomStack.byItemStack(item);
        return cs == null ? null : UMaterial.fromItemsAdder(cs.getNamespacedID());
    }
}
