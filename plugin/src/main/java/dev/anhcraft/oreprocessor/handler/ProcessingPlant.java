package dev.anhcraft.oreprocessor.handler;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.OreTransform;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.event.AsyncPlayerDataLoadEvent;
import dev.anhcraft.oreprocessor.api.event.OreMineEvent;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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

import java.util.Iterator;

public class ProcessingPlant implements Listener {
    private final OreProcessor plugin;

    public ProcessingPlant(OreProcessor plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerDataLoad(AsyncPlayerDataLoadEvent event) {
        for (String oreId : event.getData().listOreIds()) {
            OreData oreData = event.getData().requireOreData(oreId);

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
                int mul = (int) (hibernationTime / OreProcessor.getApi().getProcessingInterval());
                plugin.debug("Processing hibernated materials for %s, time = %ds, multi = x%d", event.getPlayerId(), hibernationTime, mul);

                if (!plugin.mainConfig.behaviourSettings.disableOfflineProcessing) {
                    for (String oreId : event.getData().listOreIds()) {
                        OreTransform oreTransform = OreProcessor.getApi().requireOre(oreId).getBestTransform(event.getPlayerId());
                        int processed = event.getData().requireOreData(oreId).process(mul, oreTransform::convert);
                        if (processed == 0) continue;

                        StatisticHelper.increaseProductCount(oreId, processed, event.getData());
                        StatisticHelper.increaseProductCount(oreId, processed, OreProcessor.getApi().getServerData());
                        OreProcessor.getInstance().debug(2, String.format(
                                "Processed x%d %s for %s using transform #%s",
                                processed, oreId, event.getPlayerId(), oreTransform.getId()
                        ));
                    }
                }

                // then reset hibernation to prevent any unexpected accidents causing duplication
                event.getData().setHibernationStart(0);
            }
        }

        if (plugin.mainConfig.purgeStats.maxPlayerRecords > 0) {
            OreProcessor.getInstance().debug(String.format(
                    "Removed %d oldest statistics records from player %s",
                    event.getData().purgeHourlyStats(plugin.mainConfig.purgeStats.maxPlayerRecords), event.getPlayerId()
            ));
        }
    }

    public void reload() {
        new MineralProcessingTask().runTaskTimerAsynchronously(plugin, 0, (long) (20L * OreProcessor.getApi().getProcessingInterval()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    private void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (plugin.mainConfig.whitelistWorlds != null &&
                !plugin.mainConfig.whitelistWorlds.isEmpty() &&
                !plugin.mainConfig.whitelistWorlds.contains(player.getWorld().getName()))
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isEmpty(item) || (!plugin.mainConfig.behaviourSettings.processSilkTouchItems && item.containsEnchantment(Enchantment.SILK_TOUCH)))
            return;

        Ore ore = OreProcessor.getApi().getBlockOre(event.getBlock().getType());
        if (ore == null) return;

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        OreData oreData = playerData.getOreData(ore.getId());
        boolean isFull = oreData != null && oreData.isFull();
        Bukkit.getPluginManager().callEvent(new OreMineEvent(player, event.getBlock(), ore, isFull));

        if (!isFull || plugin.mainConfig.behaviourSettings.enableMiningStatOnFullStorage) {
            StatisticHelper.increaseMiningCount(ore.getId(), playerData);
            StatisticHelper.increaseMiningCount(ore.getId(), OreProcessor.getApi().getServerData());
        }

        if (isFull) {
            if (plugin.mainConfig.behaviourSettings.dropOnFullStorage)
                return;

            OreProcessor.getInstance().msg(player, OreProcessor.getInstance().messageConfig.storageFull);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (ItemUtil.isEmpty(item) || item.containsEnchantment(Enchantment.SILK_TOUCH))
            return;

        BlockState brokenBlock = event.getBlockState();
        Ore ore = OreProcessor.getApi().getBlockOre(brokenBlock.getType());
        if (ore == null) return;

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        OreData oreData = playerData.requireOreData(ore.getId());
        if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
            return;

        boolean has = false;

        for (Iterator<Item> iterator = event.getItems().iterator(); iterator.hasNext(); ) {
            Item eventItem = iterator.next();
            Material feedstock = eventItem.getItemStack().getType();
            int amount = eventItem.getItemStack().getAmount();

            if (ore.isAcceptableFeedstock(feedstock)) {
                StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
                StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
                oreData.addFeedstock(feedstock, amount);
                has = true;
                iterator.remove();
            }
        }

        if (has && !playerData.isTutorialHidden()) {
            for (String msg : OreProcessor.getInstance().messageConfig.firstTimeTutorial) {
                OreProcessor.getInstance().rawMsg(player, msg);
            }
        }
    }
}
