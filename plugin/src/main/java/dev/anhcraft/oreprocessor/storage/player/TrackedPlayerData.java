package dev.anhcraft.oreprocessor.storage.player;

import dev.anhcraft.oreprocessor.api.data.PlayerData;
import org.jetbrains.annotations.NotNull;

public class TrackedPlayerData {
    private final PlayerData playerData;
    private long loadTime;

    public TrackedPlayerData(@NotNull PlayerData playerData, long loadTime) {
        this.playerData = playerData;
        this.loadTime = loadTime;
    }

    @NotNull
    public PlayerData getPlayerData() {
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
