package dev.anhcraft.oreprocessor.util;

import dev.anhcraft.config.bukkit.utils.ColorUtil;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.MaterialClass;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class MaterialUtil {
    public static ItemBuilder mergeToBuilder(ItemBuilder itemBuilder, UMaterial material) {
        return ItemBuilder.of(mergeToItem(itemBuilder, material));
    }

    public static ItemStack mergeToItem(ItemBuilder itemBuilder, UMaterial material) {
        if (material.getClassifier() == MaterialClass.VANILLA) {
            itemBuilder.material(Material.valueOf(material.getIdentifier()));
            return itemBuilder.build();
        }
        ItemStack item = OreProcessor.getApi().buildItem(material);
        if (item == null)
            return itemBuilder.build();
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return itemBuilder.build();
        meta.setLore(itemBuilder.lore().stream().map(ColorUtil::colorize).toList());

        Map<Enchantment, Integer> enc = itemBuilder.enchantments();
        if (enc != null) {
            for (Map.Entry<Enchantment, Integer> e : enc.entrySet()) {
                meta.addEnchant(e.getKey(), e.getValue(), true);
            }
        }

        List<ItemFlag> flags = itemBuilder.flags();
        if (flags != null)
            meta.addItemFlags(flags.toArray(new ItemFlag[0]));

        item.setItemMeta(meta);
        return item;
    }
}
