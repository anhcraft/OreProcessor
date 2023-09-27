package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.config.bukkit.utils.ColorUtil;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.event.OrePickupEvent;
import dev.anhcraft.oreprocessor.util.LocaleUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class PickupTracker implements Listener {
    private final OreProcessor plugin;
    private final Map<UUID, PlayerPickupQueue> cache = new ConcurrentHashMap<>();

    public PickupTracker(OreProcessor plugin) {
        this.plugin = plugin;
    }

    public void process() {
        for (Map.Entry<UUID, PlayerPickupQueue> ent : cache.entrySet()) {
            PickupAggregator aggregator = ent.getValue().poll();
            if (aggregator != null) {
                Player player = Bukkit.getPlayer(ent.getKey());
                String msg = ColorUtil.colorize(plugin.mainConfig.pickupTracker.message
                        .replace("{amount}", Integer.toString(aggregator.getAmount()))
                        .replace("{item}", LocaleUtils.getLocalizedName(aggregator.getMaterial())));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void pickup(OrePickupEvent event) {
        cache.computeIfAbsent(event.getPlayer().getUniqueId(), uuid -> new PlayerPickupQueue())
                .increase(event.getFeedstock(), event.getAmount());
    }

    @EventHandler
    private void quit(PlayerQuitEvent event) {
        cache.remove(event.getPlayer().getUniqueId());
    }

    public static class PlayerPickupQueue {
        private final PriorityBlockingQueue<PickupAggregator> queue = new PriorityBlockingQueue<>();

        public PickupAggregator poll() {
            return queue.poll();
        }

        public void increase(Material material, int amount) {
            for (PickupAggregator m : queue) {
                if (m.getMaterial() == material) {
                    m.addAmount(amount);
                    return;
                }
            }
            queue.add(new PickupAggregator(material, amount));
        }
    }

    public static class PickupAggregator implements Comparable<PickupAggregator> {
        private final Material material;
        private int amount;

        public PickupAggregator(Material material, int amount) {
            this.material = material;
            this.amount = amount;
        }

        public Material getMaterial() {
            return material;
        }

        public int getAmount() {
            return amount;
        }

        public void addAmount(int amount) {
            this.amount += amount;
        }

        @Override
        public int compareTo(@NotNull PickupTracker.PickupAggregator o) {
            return Integer.compare(o.amount, this.amount); // descending
        }
    }
}
