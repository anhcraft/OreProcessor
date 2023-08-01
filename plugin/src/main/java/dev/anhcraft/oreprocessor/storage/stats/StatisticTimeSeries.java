package dev.anhcraft.oreprocessor.storage.stats;

import dev.anhcraft.config.annotations.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Configurable
public class StatisticTimeSeries {
    private StatisticConfig cumulativeStats;
    private TreeMap<Long, StatisticConfig> hourlyStats;

    @NotNull
    public StatisticConfig getCumulativeStats() {
        if (cumulativeStats == null)
            cumulativeStats = new StatisticConfig(); // don't mark dirty (unneeded)

        return cumulativeStats;
    }

    @NotNull
    public StatisticConfig getOrCreateHourlyStat(long timestamp) {
        if (hourlyStats == null)
            hourlyStats = new TreeMap<>(); // don't mark dirty (unneeded)

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

    public int purgeHourlyStats(int maxRecords) {
        if (hourlyStats == null || maxRecords < 1) return 0;
        if (maxRecords >= hourlyStats.size()) {
            return 0;
        }

        int entriesToRemove = hourlyStats.size() - maxRecords;

        Iterator<Map.Entry<Long, StatisticConfig>> iterator = hourlyStats.entrySet().iterator();

        for (int i = 0; i < entriesToRemove && iterator.hasNext(); i++) {
            iterator.next();
            iterator.remove();
        }

        return entriesToRemove;
    }
}
