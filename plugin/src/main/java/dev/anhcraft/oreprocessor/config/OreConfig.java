package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.jvmkit.utils.EnumUtil;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.WheelSelection;
import org.bukkit.Material;

import java.util.*;
import java.util.stream.Collectors;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
@Example("coal:\n" +
        "  name: \"Coal\"\n" +
        "  icon: coal\n" +
        "  blocks:\n" +
        "    - coal_ore\n" +
        "    - deepslate_coal_ore\n" +
        "  transform:\n" +
        "    default:\n" +
        "      - coal > coal"
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

    public LinkedHashMap<String, Map<Material, WheelSelection<Material>>> transform = new LinkedHashMap<>();

    @PostHandler
    private void postProcess() {
        blocks = blocks.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        for (Map.Entry<String, List<String>> e : rawTransform.entrySet()) {
            Map<Material, WheelSelection<Material>> map = new EnumMap<>(Material.class);
            for (String str : e.getValue()) {
                String[] split = str.split(">");
                if (split.length != 2) continue;
                Material from = (Material) EnumUtil.findEnum(Material.class, split[0].trim().toUpperCase());
                WheelSelection<Material> to = parseSelectionSet(split[1]);
                if (from == null || to.isEmpty())
                    continue;

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

    private WheelSelection<Material> parseSelectionSet(String str) {
        WheelSelection<Material> map = new WheelSelection<>();

        String[] choices = str.trim().split(",");
        for (String choice : choices) {
            String[] args = choice.trim().split("\\s+");

            if (args.length == 1) {
                Material material = (Material) EnumUtil.findEnum(Material.class, args[0].toUpperCase());
                if (material != null)
                    map.add(material, 100d);
                else
                    OreProcessor.getInstance().getLogger().warning(String.format("Unknown material '%s' in phase '%s'", args[0], str));
            }

            else if (args.length == 2) {
                Material material = (Material) EnumUtil.findEnum(Material.class, args[1].toUpperCase());
                if (material != null)
                    map.add(material, Double.parseDouble(args[0].replace("%", "")));
                else
                    OreProcessor.getInstance().getLogger().warning(String.format("Unknown material '%s' in phase '%s'", args[0], str));
            }

            else {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid choice format '%s' in phase '%s'", choice, str));
            }
        }

        return map;
    }
}
