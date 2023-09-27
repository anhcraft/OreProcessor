package dev.anhcraft.oreprocessor.api.event;

import dev.anhcraft.oreprocessor.api.Ore;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when ores are picked up and will be transferred to the storage.<br>
 * This event is also called even when the ore was considered unsuitable, in this case {@link #isCancelled()} will return true.
 * Set {@link Cancellable#setCancelled(boolean)} to bypass this.
 */
public class OrePickupEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Block block;
    private final BlockState brokenState;
    private final Ore ore;
    private Material feedstock;
    private int amount;
    private boolean cancelled;

    public OrePickupEvent(Player player, Block block, BlockState brokenState, Ore ore, Material feedstock, int amount) {
        super(player);
        this.block = block;
        this.brokenState = brokenState;
        this.ore = ore;
        this.feedstock = feedstock;
        this.amount = amount;
    }

    @NotNull
    public Block getBlock() {
        return block;
    }

    @NotNull
    public BlockState getBrokenState() {
        return brokenState;
    }

    @NotNull
    public Ore getOre() {
        return ore;
    }

    @NotNull
    public Material getFeedstock() {
        return feedstock;
    }

    public void setFeedstock(@NotNull Material feedstock) {
        this.feedstock = feedstock;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
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
