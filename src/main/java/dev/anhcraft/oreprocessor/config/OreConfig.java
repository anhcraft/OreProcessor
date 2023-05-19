package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
@Example(
        "  coal:\n" +
        "    name: \"Coal\"\n" +
        "    blocks:\n" +
        "      - coal_ore\n" +
        "    raw-materials:\n" +
        "      - coal"
)
public class OreConfig {

    @Description("This ore's name")
    @Validation(notNull = true)
    public String name;

    @Description({
            "A list of allowed blocks",
            "This option exists to prevent conflicts with other custom drop plugins"
    })
    public List<Material> blocks = Collections.emptyList();

    @Description("A list of allowed raw materials that will be converted to the product")
    public List<Material> rawMaterials = Collections.emptyList();

    @PostHandler
    private void postProcess() {
        blocks = blocks.stream().filter(Objects::nonNull).collect(Collectors.toList());
        rawMaterials = rawMaterials.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
