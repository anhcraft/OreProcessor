package dev.anhcraft.oreprocessor.integration;

import com.archyx.aureliumskills.api.event.PlayerLootDropEvent;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Collection;

public class AureliumSkillsBridge implements Integration, Listener {
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

        for (Ore ore : ores) {
            OreData oreData = playerData.requireOreData(ore.getId());
            if (oreData.isFull()) continue;

            oreData.addFeedstock(feedstock, amount);
            event.setCancelled(true);
            break; // add once only
        }
    }
}
