package dev.anhcraft.oreprocessor.integration;

import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
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
import java.util.Map;

public class AureliumSkillsBridge implements Integration, Listener, EventDebugger {
    private final OreProcessor plugin;

    public AureliumSkillsBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onDropLoot(PlayerLootDropEvent event) {
        Player player = event.getPlayer();
        Material feedstock = event.getItemStack().getType();
        int amount = event.getItemStack().getAmount();
        Collection<Ore> ores = OreProcessor.getApi().getOresAllowFeedstock(feedstock);
        if (ores.isEmpty()) return;

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
            event.setCancelled(true);
            event.setItemStack(new ItemStack(Material.AIR));
            break; // add once only
        }

        if (isFull && !plugin.mainConfig.behaviourSettings.dropOnFullStorage)
            event.setCancelled(true);
    }

    @Override
    public Map<String, HandlerList> getEventHandlers() {
        return Collections.singletonMap("PlayerLootDropEvent", PlayerLootDropEvent.getHandlerList());
    }
}
