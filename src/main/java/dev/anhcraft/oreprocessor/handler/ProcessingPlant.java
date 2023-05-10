package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevel;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProcessingPlant implements Listener {
    private final OreProcessor plugin;
    public Map<Material, Material> rawToProductMap;
    public Set<Material> allowedBlocks;
    private TreeMap<Integer, UpgradeLevel> throughputUpgrades;
    private TreeMap<Integer, UpgradeLevel> capacityUpgrade;

    public ProcessingPlant(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void refresh() {
        rawToProductMap = new EnumMap<>(Material.class);
        allowedBlocks = EnumSet.noneOf(Material.class);

        for (Map.Entry<Material, OreConfig> entry : plugin.mainConfig.ores.entrySet()) {
            for (Material rawMaterial : entry.getValue().rawMaterials) {
                rawToProductMap.put(rawMaterial, entry.getKey());
            }
            allowedBlocks.addAll(entry.getValue().blocks);
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
    private void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        BlockState brokenBlock = event.getBlockState();
        if (!allowedBlocks.contains(brokenBlock.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.containsEnchantment(Enchantment.SILK_TOUCH)) return;

        for (Iterator<Item> iterator = event.getItems().iterator(); iterator.hasNext(); ) {
            Item eventItem = iterator.next();
            if (rawToProductMap.containsKey(eventItem.getItemStack().getType())) {

                iterator.remove();
            }
        }
    }
}
