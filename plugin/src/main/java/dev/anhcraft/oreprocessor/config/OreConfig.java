package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.jvmkit.utils.EnumUtil;
import org.bukkit.Material;

import java.util.*;
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

    @Description("This ore's icon")
    @Validation(notNull = true)
    public Material icon;

    @Description({
            "A list of allowed blocks",
            "This option exists to prevent conflicts with other custom drop plugins"
    })
    @Validation(notNull = true, notEmpty = true)
    public Set<Material> blocks = Collections.emptySet();

    @Description({
            "A list of categories of material transformation",
            "The key is used for permission",
            "A <b>default</b> key must always exist and will not require permission"
    })
    @Path("transform")
    private LinkedHashMap<String, List<String>> rawTransform;

    public LinkedHashMap<String, Map<Material, Material>> transform = new LinkedHashMap<>();

    @PostHandler
    private void postProcess() {
        blocks = blocks.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        for (Map.Entry<String, List<String>> e : rawTransform.entrySet()) {
            Map<Material, Material> map = new EnumMap<>(Material.class);
            for (String str : e.getValue()) {
                String[] split = str.split(">");
                if (split.length != 2) continue;
                Material from = (Material) EnumUtil.findEnum(Material.class, split[0].trim().toUpperCase());
                Material to = (Material) EnumUtil.findEnum(Material.class, split[1].trim().toUpperCase());
                if (from == null || to == null) continue;
                map.put(from, to);
            }
            transform.put(e.getKey(), map);
        }

        if (!transform.containsKey("default")) {
            throw new RuntimeException("Default transform must always exist");
        }

        if (!transform.keySet().iterator().next().equals("default")) {
            throw new RuntimeException("Default transform must be at first");
        }
    }
}