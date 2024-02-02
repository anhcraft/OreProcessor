package dev.anhcraft.oreprocessor.util;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialUtil {
    public static void apply(ItemBuilder itemBuilder, UMaterial material) {
        if (material.getClassifier() == MaterialClass.VANILLA) {
            itemBuilder.material(Material.valueOf(material.getIdentifier()));
            return;
        }
        ItemStack item = OreProcessor.getApi().buildItem(material);
        if (item == null)
            return;
        // TODO more data support
        ItemBuilder builder = ItemBuilder.of(item);
        itemBuilder.material(builder.material());
        itemBuilder.customModelData(builder.customModelData());
    }
}
