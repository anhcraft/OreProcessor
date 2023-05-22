package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevel;
import dev.anhcraft.oreprocessor.storage.data.PlayerDataConfig;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProcessingPlant implements Listener {
    private final OreProcessor plugin;
    public Map<Material, Material> rawToProductMap;
    public Map<Material, Material> blockToProductMap;
    private TreeMap<Integer, UpgradeLevel> throughputUpgrades;
    private TreeMap<Integer, UpgradeLevel> capacityUpgrade;

    public ProcessingPlant(OreProcessor plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.playerDataManager.setOnPlayerDataLoad(p -> {
            validateUpgrade(p.getFirst(), p.getSecond());
            processHibernation(p.getFirst(), p.getSecond());
        });
    }

    private void validateUpgrade(UUID who, PlayerDataConfig playerData) {
        for (Material product : plugin.mainConfig.ores.keySet()) {
            int throughput = playerData.getThroughput(product);
            int defaultThroughput = plugin.getDefaultThroughput();
            if (throughput < defaultThroughput) {
                playerData.setThroughput(product, defaultThroughput);
                plugin.debug("Upgrade %s's %s throughput to default value: %d → %d", who, product, throughput, defaultThroughput);
            }

            int capacity = playerData.getCapacity(product);
            int defaultCapacity = plugin.getDefaultCapacity();
            if (capacity < defaultCapacity) {
                playerData.setCapacity(product, defaultCapacity);
                plugin.debug("Upgrade %s's %s capacity to default value: %d → %d", who, product, capacity, defaultCapacity);
            }
        }
    }

    private void processHibernation(UUID who, PlayerDataConfig playerData) {
        if (playerData.hibernationStart == 0) return;
        long hibernationTime = (System.currentTimeMillis() - playerData.hibernationStart) / 1000;
        if (hibernationTime <= 0) return;
        int mul = (int) (hibernationTime / plugin.mainConfig.processingSpeed);
        plugin.debug("Processing hibernated ore for %s, time = %ds, multi = x%d", who, hibernationTime, mul);
        playerData.processOre(mul);
        // After done, reset hibernation to zero, to prevent any unexpected accidents from causing duplications
        playerData.hibernationStart = 0;
        playerData.markDirty();
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
        for (UpgradeLevel upgradeLevel : plugin.upgradeConfig.throughputUpgrade.values()) {
            throughputUpgrades.put(upgradeLevel.amount, upgradeLevel);
        }

        capacityUpgrade = new TreeMap<>();
        for (UpgradeLevel upgradeLevel : plugin.upgradeConfig.capacityUpgrade.values()) {
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
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        Block block = event.getBlock();
        if (!blockToProductMap.containsKey(block.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isEmpty(item) || item.containsEnchantment(Enchantment.SILK_TOUCH)) return;
        PlayerDataConfig playerData = plugin.playerDataManager.getData(player);
        Material product = blockToProductMap.get(block.getType());
        if (playerData.isStorageFull(product)) {
            OreProcessor.getInstance().msg(player, OreProcessor.getInstance().messageConfig.storageFull);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) return;
        BlockState brokenBlock = event.getBlockState();
        if (!blockToProductMap.containsKey(brokenBlock.getType())) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isEmpty(item) || item.containsEnchantment(Enchantment.SILK_TOUCH)) return;
        PlayerDataConfig playerData = plugin.playerDataManager.getData(player);

        boolean has = false;

        for (Iterator<Item> iterator = event.getItems().iterator(); iterator.hasNext(); ) {
            Item eventItem = iterator.next();
            Material product = rawToProductMap.get(eventItem.getItemStack().getType());

            if (product != null) {
                playerData.queueOre(product, eventItem.getItemStack().getAmount());
                has = true;
                iterator.remove();
            }
        }

        if (has && !playerData.hideTutorial) {
            for (String msg : OreProcessor.getInstance().messageConfig.firstTimeTutorial) {
                OreProcessor.getInstance().msg(player, msg);
            }
        }
    }
}
