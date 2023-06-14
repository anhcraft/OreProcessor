package dev.anhcraft.oreprocessor.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an ore transformation in which feedstock undergoes a process and turns into products.
 */
public class OreTransform {
    private final Map<Material, Material> transformMap;

    public OreTransform(Map<Material, Material> transformMap) {
        this.transformMap = transformMap; // unmodifiable
    }

    @NotNull
    public Set<Material> getFeedstock() {
        return transformMap.keySet(); // unmodifiable
    }

    @NotNull
    public Set<Material> getProducts() {
        return new HashSet<>(transformMap.values()); // unmodifiable
    }

    public boolean hasFeedstock(Material material) {
        return transformMap.containsKey(material);
    }

    public boolean hasProduct(Material material) {
        return transformMap.containsValue(material);
    }

    /**
     * Converts the given feedstock to its corresponding product.
     * @param feedstock the feedstock
     * @return the corresponding product or {@code null} if not exists
     */
    @Nullable
    public Material convert(Material feedstock) {
        return transformMap.get(feedstock);
    }
}
