package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.util.UItemStack;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.util.CraftingRecipe;
import dev.anhcraft.palette.ui.Gui;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class CraftingGui extends Gui {
    @Description("Product representation")
    @Validation(notNull = true)
    private ItemBuilder craftableProductIcon;

    @Description("Product representation")
    @Validation(notNull = true)
    private ItemBuilder uncraftableProductIcon;

    @Description("Recipes")
    @Validation(notNull = true)
    private List<String> recipes = Collections.emptyList();

    @Exclude
    private final Map<UMaterial, CraftingRecipe> craftingRecipeMap = new HashMap<>();

    public ItemBuilder getCraftableProductIcon() {
        return craftableProductIcon.duplicate();
    }

    public ItemBuilder getUncraftableProductIcon() {
        return uncraftableProductIcon.duplicate();
    }

    private boolean isEmpty(Material value) {
        return value == null || value == Material.AIR || value.name().endsWith("_AIR");
    }

    @PostHandler
    private void postProcess() {
        for (String recipe : recipes) {
            String[] args = recipe.split(">");
            if (args.length != 2) {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid crafting recipe phase '%s'", recipe));
                continue;
            }
            UItemStack in = parseItem(args[0].trim());
            if (in == null) {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid crafting input in phase '%s'", recipe));
                continue;
            }
            UItemStack out = parseItem(args[1].trim());
            if (out == null) {
                OreProcessor.getInstance().getLogger().warning(String.format("Invalid crafting output in phase '%s'", recipe));
                continue;
            }
            craftingRecipeMap.put(in.material(), new CraftingRecipe(in, out));
        }
    }

    private UItemStack parseItem(String str) {
        String[] args = str.split("\\s+");
        if (args.length != 2) {
            OreProcessor.getInstance().getLogger().warning(String.format("Invalid crafting recipe phase '%s'", str));
            return null;
        }
        if (!args[0].matches("^\\d+$")) {
            OreProcessor.getInstance().getLogger().warning(String.format("Invalid number '%s' in phase '%s'", args[0], str));
            return null;
        }
        UMaterial material = UMaterial.parse(args[1].toUpperCase());
        if (material == null) {
            OreProcessor.getInstance().getLogger().warning(String.format("Invalid material '%s' in phase '%s'", args[1], str));
            return null;
        }
        return new UItemStack(material, Integer.parseInt(args[0]));
    }

    @Nullable
    public CraftingRecipe getRecipeFor(UMaterial product) {
        return craftingRecipeMap.get(product);
    }
}
