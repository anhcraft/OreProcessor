package dev.anhcraft.oreprocessor.integration.adder;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.integration.Integration;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class OraxenBridge implements Integration, ItemCustomizer {
    private final OreProcessor plugin;

    public OraxenBridge(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Override
    public MaterialClass getMaterialClass() {
        return MaterialClass.ORAXEN;
    }

    @Override
    public Set<UMaterial> getCustomMaterials() {
        return OraxenItems.nameStream().map(UMaterial::fromOraxen).collect(Collectors.toSet());
    }

    @Override
    public @NotNull ItemStack buildItem(@NotNull UMaterial material) {
        return OraxenItems.getItemById(material.getIdentifier()).build();
    }

    @Override
    public UItemStack identifyItem(@NotNull ItemStack item) {
        String id = OraxenItems.getIdByItem(item);
        return id == null ? null : new UItemStack(UMaterial.fromOraxen(id), item.getAmount());
    }

    @Override
    public UMaterial identifyMaterial(@NotNull ItemStack item) {
        String id = OraxenItems.getIdByItem(item);
        return id == null ? null : UMaterial.fromOraxen(id);
    }
}
