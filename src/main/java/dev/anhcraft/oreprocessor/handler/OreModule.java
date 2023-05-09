package dev.anhcraft.oreprocessor.handler;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;

public class OreModule {
    private final Material icon;
    private final List<Material> validBlocks;
    private final Map<Material, Material> replacements;

    public OreModule(Material icon, List<Material> validBlocks, Map<Material, Material> replacements) {
        this.icon = icon;
        this.validBlocks = validBlocks;
        this.replacements = replacements;
    }

    public Material getIcon() {
        return icon;
    }

    public List<Material> getValidBlocks() {
        return validBlocks;
    }

    public Map<Material, Material> getReplacements() {
        return replacements;
    }
}
