package dev.anhcraft.oreprocessor.storage.player.db;

import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.data.stats.Statistics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DBPlayerData implements PlayerData {
    private final String  uuid;
    private final DatabaseManager databaseManager;

    public DBPlayerData(String uuid, DatabaseManager databaseManager) {
        this.uuid = uuid;
        this.databaseManager = databaseManager;
    }

    @Override
    public int getDataVersion() {
        return 0;
    }

    @Override
    public boolean isTutorialHidden() {

    }

    @Override
    public void hideTutorial(boolean value) {

    }

    @Override
    public @NotNull List<String> listOreIds() {

    }

    @Override
    public @Nullable OreData getOreData(@NotNull String ore) {

    }

    @Override
    public @NotNull synchronized OreData requireOreData(@NotNull String ore) {

    }

    @Override
    public long getHibernationStart() {

    }

    @Override
    public void setHibernationStart(long hibernationStart) {

    }

    @Override
    public @NotNull Statistics getCumulativeStats() {

    }

    @Override
    public @Nullable Statistics getHourlyStats(long timestamp) {

    }

    @Override
    public @NotNull Statistics getOrCreateHourlyStats(long timestamp) {

    }

    @Override
    public int purgeHourlyStats(int maxRecords) {

    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
