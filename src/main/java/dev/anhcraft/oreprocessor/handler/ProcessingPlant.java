package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevel;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import org.bukkit.GameMode;
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

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

public class ProcessingPlant implements Listener {
    private final OreProcessor plugin;
    public Map<Material, Material> rawToProductMap;
    public Map<Material, Material> blockToProductMap;
    private TreeMap<Integer, UpgradeLevel> throughputUpgrades;
    private TreeMap<Integer, UpgradeLevel> capacityUpgrade;

    public ProcessingPlant(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {
        new MineralProcessingTask().runTaskTimerAsynchronously(plugin, 0, 20L * plugin.mainConfig.processingSpeed);

        rawToProductMap = new EnumMap<>(Material.class);
        blockToProductMap = new EnumMap<>(Material.class);

        for (Map.Entry<Material, OreConfig> entry : plugin.mainConfig.ores.entrySet()) {
            for (Material rawMaterial : entry.getValue().rawMaterials) {
                rawToProductMap.put(rawMaterial, entry.getKey());
            }
            for (Material block : entry.getValue().blocks) {
                blockToProductMap.put(block, entry.getKey());
            }
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

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        if (!blockToProductMap.containsKey(block.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.containsEnchantment(Enchantment.SILK_TOUCH)) return;
        PlayerData playerData = plugin.playerDataManager.getData(player);
        Material product = blockToProductMap.get(block.getType());
        if (playerData.isStorageFull(product)) {
            OreProcessor.getInstance().msg(player, OreProcessor.getInstance().messageConfig.storageFull);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    private void onPostBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        Block block = event.getBlock();
        if (!blockToProductMap.containsKey(block.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.containsEnchantment(Enchantment.SILK_TOUCH)) return;
        PlayerData playerData = plugin.playerDataManager.getData(player);

        boolean has = false;

        for (ItemStack drop : block.getDrops(item)) {
            Material product = rawToProductMap.get(drop.getType());
            if (product != null) {
                playerData.queueOre(product, drop.getAmount());
                has = true;
            } else {
                block.getWorld().dropItemNaturally(block.getLocation(), drop);
            }
        }

        if (has) {
            if (!playerData.hideTutorial) {
                for (String msg : OreProcessor.getInstance().messageConfig.firstTimeTutorial) {
                    OreProcessor.getInstance().msg(player, msg);
                }
            }

            event.setDropItems(false);
            event.setCancelled(true);
            block.setType(Material.AIR);
        }
    }
}
