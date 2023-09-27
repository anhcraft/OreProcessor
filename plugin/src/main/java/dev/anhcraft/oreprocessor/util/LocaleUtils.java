package dev.anhcraft.oreprocessor.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.EnumMap;

public class LocaleUtils {
    private static final EnumMap<Material, String> CACHE = new EnumMap<>(Material.class);

    public static String getLocalizedName(Material material) {
        if (CACHE.containsKey(material))
            return CACHE.get(material);
        String name = snakeCaseToTitleCase(material.name());
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName())
                name = meta.getDisplayName();
            else if (meta.hasLocalizedName())
                name = meta.getLocalizedName();
        }
        CACHE.put(material, name);
        return name;
    }

    public static String snakeCaseToTitleCase(String str) {
        StringBuilder result = new StringBuilder();
        boolean b = true;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                if (!b) result.append(' ');
                b = true;
            } else if (b) {
                result.append(Character.toUpperCase(c));
                b = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }
}
