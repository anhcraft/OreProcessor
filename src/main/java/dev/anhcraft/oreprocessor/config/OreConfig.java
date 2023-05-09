package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.oreprocessor.util.OreTransform;
import org.bukkit.Material;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class OreConfig {
    public Material icon;
    public List<Material> blocks;
    public List<OreTransform> transform;
}
