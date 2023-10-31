package dev.anhcraft.oreprocessor.api.util;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * An abstract wrapper to workaround with different materials from vanilla and custom item plugins.
 */
public class UMaterial {
    public static final UMaterial EMPTY = UMaterial.fromVanilla(Material.AIR);

    private final String identifier;
    private final MaterialClass classifier;

    @Nullable
    public static UMaterial parse(@NotNull String str) {
        String[] args = str.split(":");
        if (args.length > 2) {
            throw new IllegalArgumentException("Invalid material: " + str);
        }
        if (args.length == 2) {
            MaterialClass materialClass = MaterialClass.getClassByPrefix(args[0]);
            if (materialClass == null) {
                throw new IllegalArgumentException("Invalid material class: " + str);
            }
            return new UMaterial(materialClass, args[1]);
        }
        Material material = Material.getMaterial(str.toUpperCase());
        return material == null ? null : fromVanilla(material);
    }

    public static UMaterial fromVanilla(@NotNull Material material) {
        return new UMaterial(MaterialClass.VANILLA, material.name());
    }

    public static UMaterial fromOraxen(@NotNull String material) {
        return new UMaterial(MaterialClass.ORAXEN, material);
    }

    public static UMaterial fromItemsAdder(@NotNull String material) {
        return new UMaterial(MaterialClass.ITEMSADDER, material);
    }

    public UMaterial(@NotNull MaterialClass classifier, @NotNull String identifier) {
        this.identifier = normalize(classifier, identifier);
        this.classifier = classifier;
    }

    private String normalize(MaterialClass classifier, String identifier) {
        return classifier == MaterialClass.VANILLA ? identifier.toUpperCase() : identifier;
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
        return classifier == MaterialClass.VANILLA && material.name().equals(identifier);
    }

    public boolean is(@NotNull MaterialClass classifier, @NotNull String identifier) {
        return this.classifier == classifier && this.identifier.equals(normalize(classifier, identifier));
    }

    public boolean is(@NotNull UMaterial material) {
        return equals(material);
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
        if (classifier.getPrefix() != null) {
            return classifier.getPrefix() + ":" + identifier;
        }
        return identifier;
    }
}
