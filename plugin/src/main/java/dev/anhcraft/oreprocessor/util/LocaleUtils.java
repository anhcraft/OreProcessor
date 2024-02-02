package dev.anhcraft.oreprocessor.util;

import dev.anhcraft.oreprocessor.api.ApiProvider;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class LocaleUtils {
    private static final Map<UMaterial, String> CACHE = new HashMap<>();

    public static String getLocalizedName(UMaterial material) {
        if (CACHE.containsKey(material))
            return CACHE.get(material);
        String name = snakeCaseToTitleCase(material.getIdentifier());
        ItemStack itemStack = ApiProvider.getApi().buildItem(material);
        if (itemStack != null) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                if (meta.hasDisplayName())
                    name = meta.getDisplayName();
                else if (meta.hasLocalizedName())
                    name = meta.getLocalizedName();
            }
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
