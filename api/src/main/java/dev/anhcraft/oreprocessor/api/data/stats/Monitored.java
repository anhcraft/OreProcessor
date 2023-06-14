package dev.anhcraft.oreprocessor.api.data.stats;

import org.jetbrains.annotations.NotNull;

public interface Monitored {
    @NotNull
    Statistics getCumulativeStats();

    @NotNull
    Statistics getHourlyStats(long timestamp);
}
