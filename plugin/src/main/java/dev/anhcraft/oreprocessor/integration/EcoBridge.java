package dev.anhcraft.oreprocessor.integration;

import com.willfp.eco.core.events.DropQueuePushEvent;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.storage.stats.StatisticHelper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class EcoBridge implements Integration, Listener, EventDebugger {
    private final OreProcessor plugin;

    public EcoBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    private void onDropLoot(DropQueuePushEvent event) {
        Player player = event.getPlayer();
        if (event.isTelekinetic()) return;

        for (Iterator<? extends ItemStack> it = event.getItems().iterator(); it.hasNext(); ) {
            ItemStack item = it.next();
            Material feedstock = item.getType();
            int amount = item.getAmount();
            Collection<Ore> ores = OreProcessor.getApi().getOresAllowFeedstock(feedstock);
            if (ores.isEmpty()) continue;

            PlayerData playerData = plugin.playerDataManager.getData(player);
            boolean isFull = false;

            for (Ore ore : ores) {
                OreData oreData = playerData.requireOreData(ore.getId());
                if (oreData.isFull()) {
                    isFull = true;
                    continue;
                }

                isFull = false;
                StatisticHelper.increaseFeedstockCount(ore.getId(), amount, playerData);
                StatisticHelper.increaseFeedstockCount(ore.getId(), amount, OreProcessor.getApi().getServerData());
                oreData.addFeedstock(feedstock, amount);
                it.remove();
                break; // add once only
            }

            if (isFull && !plugin.mainConfig.behaviourSettings.dropOnFullStorage)
                it.remove();
        }
    }

    @Override
    public Map<String, HandlerList> getEventHandlers() {
        return Collections.singletonMap("DropQueuePushEvent", DropQueuePushEvent.getHandlerList());
    }
}
