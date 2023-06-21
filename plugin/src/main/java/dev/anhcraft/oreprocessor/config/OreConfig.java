package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.jvmkit.utils.EnumUtil;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.WheelSelection;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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

    public LinkedHashMap<String, Map<Material, WheelSelection<ItemStack>>> transform = new LinkedHashMap<>();

    @PostHandler
    private void postProcess() {
        blocks = blocks.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        for (Map.Entry<String, List<String>> e : rawTransform.entrySet()) {
            Map<Material, WheelSelection<ItemStack>> map = new EnumMap<>(Material.class);
            for (String str : e.getValue()) {
                String[] split = str.split("\\s*>\\s*");
                if (split.length != 2)
                    continue;

                Material from = (Material) EnumUtil.findEnum(Material.class, split[0].trim().toUpperCase());
                if (from == null) {
                    OreProcessor.getInstance().getLogger().warning(String.format("Unknown material '%s' in phase '%s'", split[0], str));
                    continue;
                }

                WheelSelection<ItemStack> to = parseSelectionSet(split[1]);
                if (to.isEmpty()) {
                    OreProcessor.getInstance().getLogger().warning(String.format("No products available in phase '%s'", str));
                    continue;
                }

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

    private WheelSelection<ItemStack> parseSelectionSet(String str) {
        WheelSelection<ItemStack> map = new WheelSelection<>();
        str = str.trim();
        String[] choices = str.split(",");

        for (String choice : choices) {
            String[] args = choice.trim().split("\\s+");

            if (args.length == 1) {
                ItemStack is = parseItemstack(args[0]);
                if (is != null) map.add(is, 100d);
                else OreProcessor.getInstance().getLogger().warning(String.format("Invalid item format '%s' in phase '%s'", args[0], str));
            }

            else if (args.length == 2) {
                ItemStack is = parseItemstack(args[1]);
                if (is != null) map.add(is, Double.parseDouble(args[0].replace("%", "")));
                else OreProcessor.getInstance().getLogger().warning(String.format("Invalid item format '%s' in phase '%s'", args[1], str));
            }

            else {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid format '%s' in phase '%s'", choice, str));
            }
        }

        return map;
    }

    @Nullable
    private ItemStack parseItemstack(String str) {
        String[] args = str.split(":");
        if (args.length == 1) {
            Material material = (Material) EnumUtil.findEnum(Material.class, args[0].toUpperCase());
            return material == null ? null : new ItemStack(material, 1);
        } else if (args.length == 2) {
            Material material = (Material) EnumUtil.findEnum(Material.class, args[0].toUpperCase());
            String num = args[1];
            if (!num.matches("\\d+")) {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid number '%s' in phase '%s'", num, str));
                return null;
            }
            return material == null ? null : new ItemStack(material, Integer.parseInt(num));
        } else {
            return null;
        }
    }
}
