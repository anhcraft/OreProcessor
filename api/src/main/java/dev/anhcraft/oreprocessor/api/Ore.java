package dev.anhcraft.oreprocessor.api;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Ore {
    private final String name;
    private final Material icon;
    private final List<Material> blocks;
    private final Map<String, OreTransform> transform;

    public Ore(String name, Material icon, List<Material> blocks, Map<String, OreTransform> transform) {
        this.name = name;
        this.icon = icon;
        this.blocks = blocks;
        this.transform = transform;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Material getIcon() {
        return icon;
    }

    @NotNull
    public List<Material> getBlocks() {
        return Collections.unmodifiableList(blocks);
    }

    @NotNull
    public Set<String> getTransformIds() {
        return Collections.unmodifiableSet(transform.keySet());
    }

    @Nullable
    public OreTransform getTransform(String id) {
        return transform.get(id);
    }
}
