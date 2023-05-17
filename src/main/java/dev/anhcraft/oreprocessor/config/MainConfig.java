package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Validation;
import org.bukkit.Material;

import java.util.LinkedHashMap;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MainConfig {
    public boolean devMode;

    public int processingSpeed = 5;

    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<Material, OreConfig> ores;

    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<String, UpgradeLevel> throughputUpgrade;

    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<String, UpgradeLevel> capacityUpgrade;

    public String shopProvider;
}
