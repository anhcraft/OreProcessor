package dev.anhcraft.oreprocessor.api.event;

import dev.anhcraft.oreprocessor.api.Ore;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player mines an ore which is accepted by OreProcessor.
 */
@ApiStatus.Experimental
public class OreMineEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Block block;
    private final Ore ore;
    private final boolean storageFull;

    public OreMineEvent(@NotNull Player player, @NotNull Block block, @NotNull Ore ore, boolean storageFull) {
        super(player);
        this.block = block;
        this.ore = ore;
        this.storageFull = storageFull;
    }

    @NotNull
    public Block getBlock() {
        return block;
    }

    public Ore getOre() {
        return ore;
    }

    /**
     * Checks if the storage is full.<br>
     * When the storage is full, the block can't be mined.
     * @return true if the storage is full
     */
    public boolean isStorageFull() {
        return storageFull;
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
