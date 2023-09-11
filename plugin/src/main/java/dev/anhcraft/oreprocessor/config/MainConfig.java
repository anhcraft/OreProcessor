package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;
import dev.anhcraft.oreprocessor.api.integration.ShopProviderType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Set;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MainConfig {
    @Description("Enable development mode")
    public boolean devMode;

    @Description({
            "Set debug level (does not require dev mode)",
            "0: no debug messages",
            "1: common debug messages",
            "2: more debug messages",
    })
    public int debugLevel;

    @Description({
            "The interval at which the plugin will process ores (in seconds)",
            "The maximum amount of ore processed per time is: <code>processingSpeed x throughput</code>",
            "Reducing the value results in faster processing but a downgrade in performance. It is",
            "recommended to adjust throughput values instead"
    })
    public float processingInterval = 2.5f;

    @Description({
            "Ore configuration",
            "The key specifies the id",
            "The value is the ore configuration section"
    })
    @Example("ores:\n" +
            "  coal:\n" +
            "    name: \"Coal\"\n" +
            "    icon: coal\n" +
            "    blocks:\n" +
            "      - coal_ore\n" +
            "      - deepslate_coal_ore\n" +
            "    transform:\n" +
            "      default:\n" +
            "        - coal > coal"
    )
    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<String, OreConfig> ores;

    @Description({
            "Shop plugin that will be integrated for quick sell feature",
            "If a provider becomes unavailable, no exception/error will be thrown",
            "and in the GUI, the quick sell button will be disabled",
            "Available: ShopGUIPlus, EconomyShopGUI"
    })
    @Example("shop-provider: ShopGUIPlus")
    @Nullable
    public ShopProviderType shopProvider;

    @Description({
            "Set whitelisted worlds"
    })
    @Nullable
    public Set<String> whitelistWorlds;

    @NotNull
    public BehaviourConfig behaviourSettings = new BehaviourConfig();

    @NotNull
    public AccessibilitySettings accessibilitySettings = new AccessibilitySettings();

    @NotNull
    public PurgeStatsSettings purgeStats = new PurgeStatsSettings();

    @Validation(notNull = true, silent = true)
    private String dateFormat = "dd/MM/yyyy HH:mm:ss";

    @Exclude
    public SimpleDateFormat dateTimeFormat;

    @PostHandler
    private void handle() {
        dateTimeFormat = new SimpleDateFormat(dateFormat);
    }
}
