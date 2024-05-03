package dev.anhcraft.oreprocessor.integration;

import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import net.advancedplugins.ae.impl.utils.protection.events.FakeAdvancedBlockBreakEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class AdvancedEnchantmentBridge implements Integration, Listener, EventDebugger {
    private final OreProcessor plugin;

    public AdvancedEnchantmentBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    private void onFakeBreak(FakeAdvancedBlockBreakEvent event) {
        handle(event);
    }

    private void handle(FakeAdvancedBlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE)
            return;

        if (plugin.mainConfig.whitelistWorlds != null &&
                !plugin.mainConfig.whitelistWorlds.isEmpty() &&
                !plugin.mainConfig.whitelistWorlds.contains(player.getWorld().getName()))
            return;

        // Silk touch can exist along with other AE, so it is impossible to ignore here
        //if (!plugin.mainConfig.behaviourSettings.processSilkTouchItems &&
        //        player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH)) return;

        Block block = event.getBlock();
        Ore ore = OreProcessor.getApi().getBlockOre(UMaterial.of(block.getType()));
        if (ore == null) return;

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        OreData oreData = playerData.requireOreData(ore.getId());
        if (oreData.isFull() && plugin.mainConfig.behaviourSettings.dropOnFullStorage)
            return;

        Location location = block.getLocation();
        int radius = plugin.mainConfig.behaviourSettings.itemPickupRadius;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Collection<Entity> entities = location.getWorld().getNearbyEntities(location, radius, radius, radius);
            for (Entity entity : entities) {
                if (!(entity instanceof Item) || entity.isDead()) continue;
                ItemStack itemStack = ((Item) entity).getItemStack();
                if (itemStack.hasItemMeta()) continue;
                UMaterial feedstock = UMaterial.of(itemStack.getType());
                int amount = itemStack.getAmount();

                if (ore.isAcceptableFeedstock(feedstock)) {
                    StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
                    StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
                    oreData.addFeedstock(feedstock, amount);
                    entity.remove();
                }
            }
        }, 3);
    }

    @Override
    public Map<String, HandlerList> getEventHandlers() {
        return Collections.singletonMap("FakeAdvancedBlockBreakEvent", FakeAdvancedBlockBreakEvent.getHandlerList());
    }
}
