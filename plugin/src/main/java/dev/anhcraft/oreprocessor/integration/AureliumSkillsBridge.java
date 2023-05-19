package dev.anhcraft.oreprocessor.integration;

import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.data.PlayerDataConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AureliumSkillsBridge implements Integration, Listener {
    private final OreProcessor plugin;

    public AureliumSkillsBridge(OreProcessor plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onDropLoot(PlayerLootDropEvent event) {
        Player player = event.getPlayer();
        PlayerDataConfig playerData = plugin.playerDataManager.getData(player);
        Material product = plugin.processingPlant.rawToProductMap.get(event.getItemStack().getType());
        if (product != null) {
            playerData.queueOre(product, event.getItemStack().getAmount());
            event.setCancelled(true);
        }
    }
}
