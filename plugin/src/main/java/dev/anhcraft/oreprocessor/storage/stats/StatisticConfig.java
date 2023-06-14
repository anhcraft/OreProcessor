package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.config.annotations.Configurable;

import java.util.Map;

@Configurable
public class StatisticConfig {
    public Map<String, Long> miningCount;

    public Map<String, Long> feedstockCount;

    public Map<String, Long> productCount;
}
