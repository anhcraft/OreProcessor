package dev.anhcraft.oreprocessor.api;

import dev.anhcraft.oreprocessor.api.util.WheelSelection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Represents an ore transformation in which feedstock undergoes a process and turns into products.
 */
public class OreTransform {
    private final String id;
    private final Map<Material, WheelSelection<ItemStack>> transformMap;

    public OreTransform(String id, Map<Material, WheelSelection<ItemStack>> transformMap) {
        this.id = id;
        this.transformMap = transformMap; // unmodifiable
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Set<Material> getFeedstock() {
        return transformMap.keySet(); // unmodifiable
    }

    public boolean hasFeedstock(Material material) {
        return transformMap.containsKey(material);
    }

    @Nullable
    public WheelSelection<ItemStack> getProduct(Material material) {
        return transformMap.get(material);
    }

    public boolean hasProduct(Material material) {
        return transformMap.values().stream()
                .flatMap(wheelSelection -> wheelSelection.getKeys().stream())
                .anyMatch(itemStack -> itemStack.getType() == material);
    }

    /**
     * Converts the given feedstock to its corresponding product.
     * @param feedstock the feedstock
     * @return the corresponding product or {@code null} if not exists
     */
    @Nullable
    public ItemStack convert(Material feedstock) {
        WheelSelection<ItemStack> w = transformMap.get(feedstock);
        return w != null ? w.roll() : null;
    }
}
