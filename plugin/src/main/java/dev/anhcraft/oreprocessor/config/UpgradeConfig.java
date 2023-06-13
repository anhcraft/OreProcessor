package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;

import java.util.LinkedHashMap;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeConfig {
    @Description("Throughput upgrade configuration")
    @Example(
            "throughput-upgrade:\n" +
            "  default: # The default upgrade, must always exist\n" +
            "    amount: 1\n" +
            "  level-1: # Whatever name\n" +
            "    amount: 2\n" +
            "    cost: 50000\n" +
            "  level-3:\n" +
            "    amount: 3\n" +
            "    cost: 100000\n" +
            "  level-4:\n" +
            "    amount: 4\n" +
            "    cost: 300000"
    )
    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<String, UpgradeLevelConfig> throughputUpgrade;

    @Description("Capacity upgrade configuration")
    @Example(
            "capacity-upgrade:\n" +
            "  default:\n" +
            "    amount: 128\n" +
            "  level-1:\n" +
            "    amount: 192\n" +
            "    cost: 30000\n" +
            "  level-3:\n" +
            "    amount: 256\n" +
            "    cost: 50000\n" +
            "  level-4:\n" +
            "    amount: 320\n" +
            "    cost: 100000"
    )
    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<String, UpgradeLevelConfig> capacityUpgrade;

    @PostHandler
    private void handle() {
        if (!throughputUpgrade.containsKey("default") || !capacityUpgrade.containsKey("default")) {
            throw new RuntimeException("Default upgrade must always exist");
        }
    }
}
