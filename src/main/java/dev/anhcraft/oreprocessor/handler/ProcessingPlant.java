package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.UpgradeLevel;
import dev.anhcraft.oreprocessor.util.OreTransform;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class ProcessingPlant implements Listener {
    private final OreProcessor plugin;
    public List rawToProductMap;
    private TreeMap<Integer, UpgradeLevel> throughputUpgrades;
    private TreeMap<Integer, UpgradeLevel> capacityUpgrade;

    public ProcessingPlant(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void refresh() {
        rawToProductMap = new LinkedHashMap<>();
        for (OreTransform oreTransform : plugin.mainConfig.oreTransform) {
            rawToProductMap.put(oreTransform.getRaw(), oreTransform.getProduct());
        }

        throughputUpgrades = new TreeMap<>();
        for (UpgradeLevel upgradeLevel : plugin.mainConfig.throughputUpgrade.values()) {
            throughputUpgrades.put(upgradeLevel.amount, upgradeLevel);
        }

        capacityUpgrade = new TreeMap<>();
        for (UpgradeLevel upgradeLevel : plugin.mainConfig.capacityUpgrade.values()) {
            capacityUpgrade.put(upgradeLevel.amount, upgradeLevel);
        }
    }

    @Nullable
    public UpgradeLevel findNextThroughputUpgrade(int current) {
        for (Map.Entry<Integer, UpgradeLevel> entry : throughputUpgrades.entrySet()) {
            if (entry.getKey() > current) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Nullable
    public UpgradeLevel findNextCapacityUpgrade(int current) {
        for (Map.Entry<Integer, UpgradeLevel> entry : capacityUpgrade.entrySet()) {
            if (entry.getKey() > current) {
                return entry.getValue();
            }
        }
        return null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack item = player.getInventory().getItemInMainHand();
        Material product = rawToProductMap.get(block.getType());
        if (product == null || item.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        for (ItemStack drop : block.getDrops(item)) {
            if ((drop.getType() == block.getType() || drop.getType() == product) && !drop.hasItemMeta()) {

            }
        }
    }
}
