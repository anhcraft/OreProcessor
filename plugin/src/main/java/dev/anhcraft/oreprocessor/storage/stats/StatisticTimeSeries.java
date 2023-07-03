package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.config.annotations.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Configurable
public class StatisticTimeSeries {
    private StatisticConfig cumulativeStats;
    private Map<Long, StatisticConfig> hourlyStats;

    @NotNull
    public StatisticConfig getCumulativeStats() {
        if (cumulativeStats == null)
            cumulativeStats = new StatisticConfig(); // don't mark dirty (unneeded)

        return cumulativeStats;
    }

    @NotNull
    public StatisticConfig getOrCreateHourlyStat(long timestamp) {
        if (hourlyStats == null)
            hourlyStats = new HashMap<>(); // don't mark dirty (unneeded)

        long k = timestamp / 3600000L;
        StatisticConfig hourlyStat = hourlyStats.get(k);
        if (hourlyStat == null)
            hourlyStats.put(k, hourlyStat = new StatisticConfig()); // don't mark dirty (unneeded)

        return hourlyStat;
    }

    @Nullable
    public StatisticConfig getHourlyStat(long timestamp) {
        if (hourlyStats == null) return null;
        long k = timestamp / 3600000L;
        return hourlyStats.get(k);
    }
}
