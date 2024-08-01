package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.api.util.WheelSelection;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
@Example("""
  coal:
    name: "Coal"
    icon: coal
    blocks:
      - coal_ore
      - deepslate_coal_ore
    transform:
      default:
        - coal > coal"""
)
public class OreConfig {

    @Description("This ore's name")
    @Validation(notNull = true)
    public String name;

    @Description("This ore's icon")
    @Validation(notNull = true)
    public UMaterial icon;

    @Description({
            "A list of allowed blocks",
            "This option exists to prevent conflicts with other custom drop plugins"
    })
    @Validation(notNull = true, notEmpty = true, silent = true)
    public Set<UMaterial> blocks = Collections.emptySet();

    @Description({
            "A list of categories of material transformation",
            "The key is used for permission",
            "A <b>default</b> key must always exist and will not require permission"
    })
    @Path("transform")
    private LinkedHashMap<String, List<String>> rawTransform;

    @Exclude
    public LinkedHashMap<String, Map<UMaterial, WheelSelection<UItemStack>>> transform = new LinkedHashMap<>();

    @PostHandler
    private void postProcess() {
        blocks = blocks.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        for (Map.Entry<String, List<String>> e : rawTransform.entrySet()) {
            Map<UMaterial, WheelSelection<UItemStack>> map = new HashMap<>();
            for (String str : e.getValue()) {
                String[] split = str.split("\\s*>\\s*");
                if (split.length != 2)
                    continue;

                UMaterial from = UMaterial.parse(split[0].trim());
                if (from == null) {
                    OreProcessor.getInstance().getLogger().warning(String.format("Unknown material '%s' in phase '%s'", split[0], str));
                    continue;
                }

                WheelSelection<UItemStack> to = parseSelectionSet(split[1]);
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

    private WheelSelection<UItemStack> parseSelectionSet(String str) {
        WheelSelection<UItemStack> map = new WheelSelection<>();
        str = str.trim();
        String[] choices = str.split(",");

        for (String choice : choices) {
            String[] args = choice.trim().split("\\s+");

            if (args.length == 1) {
                UItemStack is = parseItemstack(args[0]);
                if (is != null) map.add(is, 100d);
                else OreProcessor.getInstance().getLogger().warning(String.format("Invalid item format '%s' in phase '%s'", args[0], str));
            }

            else if (args.length == 2) {
                UItemStack is = parseItemstack(args[1]);
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
    private UItemStack parseItemstack(String str) {
        String[] args = str.split(":");
        if (args.length == 1) {
            UMaterial material = UMaterial.parse(args[0].toUpperCase());
            return material == null ? null : new UItemStack(material, 1);
        } else if (args.length == 2) {
            UMaterial material = UMaterial.parse(args[0].toUpperCase());
            String num = args[1];
            if (!num.matches("\\d+")) {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid number '%s' in phase '%s'", num, str));
                return null;
            }
            return material == null ? null : new UItemStack(material, Integer.parseInt(num));
        } else {
            return null;
        }
    }
}
