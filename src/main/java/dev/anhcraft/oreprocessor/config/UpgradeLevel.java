package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeLevel {
    public int amount;
    public double cost;
}
