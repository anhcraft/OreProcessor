package dev.anhcraft.oreprocessor.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

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
        return transformMap.keySet(); // unmodifiable
    }

    public boolean hasFeedstock(Material material) {
        return transformMap.containsKey(material);
    }

    public boolean hasProduct(Material material) {
        return transformMap.containsValue(material);
    }

    @Nullable
    public Material convert(Material feedstock) {
        return transformMap.get(feedstock);
    }
}
