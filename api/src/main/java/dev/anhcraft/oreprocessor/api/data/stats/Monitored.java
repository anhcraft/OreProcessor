package dev.anhcraft.oreprocessor.api.data.stats;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Monitored {
    @NotNull
    Statistics getCumulativeStats();

    @Nullable
    Statistics getHourlyStats(long timestamp);

    @NotNull
    Statistics getOrCreateHourlyStats(long timestamp);

    int purgeHourlyStats(int maxRecords);
}
