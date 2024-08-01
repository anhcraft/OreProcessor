package dev.anhcraft.oreprocessor.api;

import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.api.util.WheelSelection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Represents an ore transformation in which feedstock undergoes a process and turns into products.
 */
public class OreTransform {
    private final String id;
    private final Map<UMaterial, WheelSelection<UItemStack>> transformMap;

    public OreTransform(String id, Map<UMaterial, WheelSelection<UItemStack>> transformMap) {
        this.id = id;
        this.transformMap = transformMap; // unmodifiable
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Set<UMaterial> getFeedstock() {
        return transformMap.keySet(); // unmodifiable
    }

    public boolean hasFeedstock(UMaterial material) {
        return transformMap.containsKey(material);
    }

    @Nullable
    public WheelSelection<UItemStack> getProduct(UMaterial material) {
        return transformMap.get(material);
    }

    public boolean hasProduct(UMaterial material) {
        return transformMap.values().stream()
                .flatMap(wheelSelection -> wheelSelection.getKeys().stream())
                .anyMatch(itemStack -> itemStack.material().equals(material));
    }

    /**
     * Converts the given feedstock to its corresponding product.
     * @param feedstock the feedstock
     * @return the corresponding product or {@code null} if not exists
     */
    @Nullable
    public UItemStack convert(UMaterial feedstock) {
        WheelSelection<UItemStack> w = transformMap.get(feedstock);
        return w != null ? w.roll() : null;
    }
}
