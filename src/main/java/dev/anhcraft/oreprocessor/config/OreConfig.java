package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.PostHandler;
import org.bukkit.Material;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class OreConfig {
    public String name;
    public List<Material> blocks;
    public List<Material> rawMaterials;

    @PostHandler
    private void postProcess() {
        blocks = blocks.stream().filter(Objects::nonNull).collect(Collectors.toList());
        rawMaterials = rawMaterials.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
