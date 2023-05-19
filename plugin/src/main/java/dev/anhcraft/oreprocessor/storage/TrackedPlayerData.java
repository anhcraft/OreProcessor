package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.oreprocessor.storage.data.PlayerDataConfig;
import org.jetbrains.annotations.NotNull;

public class TrackedPlayerData {
    private final PlayerDataConfig playerData;
    private long loadTime;

    public TrackedPlayerData(@NotNull PlayerDataConfig playerData, long loadTime) {
        this.playerData = playerData;
        this.loadTime = loadTime;
    }

    @NotNull
    public PlayerDataConfig getPlayerData() {
        return playerData;
    }

    public long getLoadTime() {
        return Math.abs(loadTime);
    }

    public boolean isShortTerm() {
        return loadTime > 0;
    }

    public void setShortTerm() {
        loadTime = System.currentTimeMillis();
    }

    public void setLongTerm() {
        loadTime = -System.currentTimeMillis();
    }
}
