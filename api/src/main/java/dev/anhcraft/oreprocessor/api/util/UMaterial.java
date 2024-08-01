package dev.anhcraft.oreprocessor.api.util;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An abstract wrapper to workaround with different materials from vanilla and custom item plugins.
 */
public class UMaterial {
    private static final Map<Material, UMaterial> CACHE = new EnumMap<>(Material.class);
    public static final Set<UMaterial> EMPTY = new HashSet<>(
            Arrays.asList(of(Material.AIR), of(Material.CAVE_AIR), of(Material.VOID_AIR))
    );

    @Nullable
    public static UMaterial parse(@NotNull String str) {
        String[] args = str.split(":");
        if (args.length > 2)
            return null;
        if (args.length == 2) {
            MaterialClass materialClass = MaterialClass.getClassByPrefix(args[0]);
            if (materialClass == null)
                return null;
            return new UMaterial(materialClass, args[1]);
        }
        Material material = Material.getMaterial(str.toUpperCase());
        return material == null ? null : of(material);
    }

    public static UMaterial of(@NotNull Material material) {
        return CACHE.computeIfAbsent(material, UMaterial::new);
    }

    public static UMaterial fromOraxen(@NotNull String material) {
        return new UMaterial(MaterialClass.ORAXEN, material);
    }

    public static UMaterial fromItemsAdder(@NotNull String material) {
        return new UMaterial(MaterialClass.ITEMSADDER, material);
    }

    private final String identifier;
    private final MaterialClass classifier;
    private boolean item = true;

    UMaterial(@NotNull Material material) {
        this.identifier = material.name();
        this.classifier = MaterialClass.VANILLA;
        computeProperties();
    }

    UMaterial(@NotNull MaterialClass classifier, @NotNull String identifier) {
        this.identifier = normalize(classifier, identifier);
        this.classifier = classifier;
        computeProperties();
    }

    private String normalize(MaterialClass classifier, String identifier) {
        return classifier == MaterialClass.VANILLA ? identifier.toUpperCase() : identifier;
    }

    private void computeProperties() {
        if (classifier == MaterialClass.VANILLA) {
            Material material = Material.getMaterial(identifier);
            if (material != null) {
                item = material.isItem();
            }
        }
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public MaterialClass getClassifier() {
        return classifier;
    }

    public boolean is(@NotNull Material material) {
        return this.classifier == MaterialClass.VANILLA && material.name().equals(identifier);
    }

    public boolean is(@NotNull MaterialClass classifier, @NotNull String identifier) {
        return this.classifier == classifier && this.identifier.equals(normalize(classifier, identifier));
    }

    public boolean is(@NotNull UMaterial material) {
        return equals(material);
    }

    public boolean isEmpty() {
        return EMPTY.contains(this);
    }

    public boolean isItem() {
        return item;
    }

    public Material asBukkit() {
        Preconditions.checkArgument(classifier == MaterialClass.VANILLA, "Not vanilla item");
        return Material.getMaterial(identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UMaterial)) return false;
        UMaterial uMaterial = (UMaterial) o;
        if (classifier != uMaterial.classifier) return false;
        return identifier.equals(uMaterial.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classifier, identifier);
    }

    @Override
    public String toString() {
        String prefix = classifier.getPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + ":" + identifier;
        }
        return identifier;
    }
}
