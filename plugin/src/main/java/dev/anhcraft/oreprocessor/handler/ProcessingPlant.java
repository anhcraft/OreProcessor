package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import dev.anhcraft.oreprocessor.api.event.AsyncPlayerDataLoadEvent;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevelConfig;
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

    public ProcessingPlant(OreProcessor plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDataLoad(AsyncPlayerDataLoadEvent event) {
        for (String oreId : event.getData().listOreIds()) {
            IOreData oreData = event.getData().requireOreData(oreId);

            int throughput = oreData.getThroughput();
            int defaultThroughput = OreProcessor.getApi().getDefaultThroughput();
            if (throughput < defaultThroughput) {
                oreData.setThroughput(defaultThroughput);
                plugin.debug("Upgrade %s's %s throughput to default value: %d → %d", event.getPlayerId(), oreId, throughput, defaultThroughput);
            }

            int capacity = oreData.getCapacity();
            int defaultCapacity = OreProcessor.getApi().getDefaultCapacity();
            if (capacity < defaultCapacity) {
                oreData.setCapacity(defaultCapacity);
                plugin.debug("Upgrade %s's %s capacity to default value: %d → %d", event.getPlayerId(), oreId, capacity, defaultCapacity);
            }
        }

        long hibernationStart = event.getData().getHibernationStart();
        if (hibernationStart > 0) {
            long hibernationTime = (System.currentTimeMillis() - hibernationStart) / 1000;
            if (hibernationTime > 0) {
                int mul = (int) (hibernationTime / OreProcessor.getApi().getProcessingSpeed());
                plugin.debug("Processing hibernated ore for %s, time = %ds, multi = x%d", event.getPlayerId(), hibernationTime, mul);

                for (String oreId : event.getData().listOreIds()) {
                    OreTransform oreTransform = OreProcessor.getApi().requireOre(oreId).getBestTransform(event.getPlayerId());
                    event.getData().requireOreData(oreId).process(mul, oreTransform::convert);
                }

                // then reset hibernation to prevent any unexpected accidents causing duplication
                event.getData().setHibernationStart(0);
            }
        }
    }

    public void reload() {
        new MineralProcessingTask().runTaskTimerAsynchronously(plugin, 0, 20L * plugin.mainConfig.processingSpeed);
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
