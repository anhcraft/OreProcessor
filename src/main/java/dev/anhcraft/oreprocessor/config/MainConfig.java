package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Example;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.oreprocessor.integration.shop.ShopProviderType;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MainConfig {
    @Description("Enable development mode")
    public boolean devMode;

    @Description({
            "The interval at which the plugin will process ores (in seconds)",
            "The maximum amount of ore processed per time is: <code>processingSpeed x throughput</code>",
            "Reducing the value results in faster processing time but a downgrade in performance. It is",
            "recommended to adjust throughput values instead"
    })
    public int processingSpeed = 5;

    @Description({
            "Ore configuration",
            "The key specifies the product material",
            "The value is the ore configuration section"
    })
    @Example(
            "  coal: # The product, also be the icon\n" +
            "    name: \"Coal\"\n" +
            "    blocks:\n" +
            "      - coal_ore\n" +
            "    raw-materials:\n" +
            "      - coal"
    )
    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<Material, OreConfig> ores;

    @Description({
            "Shop plugin that will be integrated for quick sell feature",
            "If a provider becomes unavailable, no exception/error will be thrown",
            "and in the GUI, the quick sell button will be disabled",
            "Available: ShopGUIPlus, EconomyShopGUI"
    })
    @Example("shop-provider: ShopGUIPlus")
    @Nullable
    public ShopProviderType shopProvider;
}
