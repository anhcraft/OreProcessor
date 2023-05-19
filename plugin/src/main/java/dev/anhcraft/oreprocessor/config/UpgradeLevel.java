package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeLevel {
    @Description("New throughput or storage-capacity value")
    public int amount;

    @Description({
            "The cost of upgrade",
            "<b>Vault integration</b>"
    })
    public double cost;
}
