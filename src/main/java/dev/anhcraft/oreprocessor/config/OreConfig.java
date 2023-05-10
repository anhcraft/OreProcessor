package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import org.bukkit.Material;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class OreConfig {
    public String name;
    public List<Material> blocks;
    public List<Material> rawMaterials;
}
