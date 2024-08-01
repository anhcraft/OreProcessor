package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.*;

import java.util.LinkedHashMap;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeConfig {
    @Description("Throughput upgrade configuration")
    @Example(
      """
        throughput-upgrade:
          default: # The default upgrade, must always exist
            amount: 1
          level-1: # Whatever name
            amount: 2
            cost: 50000
          level-3:
            amount: 3
            cost: 100000
          level-4:
            amount: 4
            cost: 300000"""
    )
    @Validation(notNull = true, notEmpty = true)
    public LinkedHashMap<String, UpgradeLevelConfig> throughputUpgrade;

    @Description("Capacity upgrade configuration")
    @Example(
      """
        capacity-upgrade:
          default:
            amount: 128
          level-1:
            amount: 192
            cost: 30000
          level-3:
            amount: 256
            cost: 50000
          level-4:
            amount: 320
            cost: 100000"""
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
