package dev.anhcraft.oreprocessor.api.event;

import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@ApiStatus.Experimental
public class AsyncPlayerDataLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final UUID uuid;
    private final IPlayerData data;

    public AsyncPlayerDataLoadEvent(@NotNull UUID uuid, @NotNull IPlayerData data) {
        super(true);
        this.uuid = uuid;
        this.data = data;
    }

    @NotNull
    public UUID getPlayerId() {
        return uuid;
    }

    @NotNull
    public IPlayerData getData() {
        return data;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
