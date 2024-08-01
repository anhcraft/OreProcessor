package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@CommandAlias("ore|oreprocessor")
public class StoreCommand extends BaseCommand {
    private final OreProcessor plugin;

    public StoreCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Subcommand("store hand")
    @Description("Add the item in your main hand into ore storage")
    @CommandPermission("oreprocessor.store.hand")
    public void storeHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (ItemUtil.isEmpty(item)) {
            plugin.msg(player, plugin.messageConfig.emptyHand);
            return;
        }

        UMaterial material = OreProcessor.getApi().identifyMaterial(item);
        if (material == null) {
            plugin.msg(player, plugin.messageConfig.storeInvalidItem);
            return;
        }
        List<Ore> ores = OreProcessor.getApi().getStorageAllowItem(material);

        if (ores == null || ores.isEmpty()) {
            plugin.msg(player, plugin.messageConfig.cannotStoreItem);
            return;
        }

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        StringJoiner oreList = new StringJoiner(",");
        int remain = item.getAmount();

        for (Ore ore : ores) {
            OreData oreData = playerData.requireOreData(ore.getId());

            int added = oreData.addProduct(material, remain, false);
            if (added == 0)
                continue;

            remain -= added;
            oreList.add(ore.getName());
            plugin.pluginLogger.scope("cmd/store/hand")
                    .add("player", player)
                    .add("item", item.getType())
                    .add("ore", ore)
                    .add("expectedDelta", remain)
                    .add("actualDelta", added)
                    .add("remain", remain)
                    .flush();

            if (remain <= 0)
                break;
        }

        plugin.msg(player, plugin.messageConfig.storedItems
                .replace("{amount}", Integer.toString(item.getAmount() - remain))
                .replace("{ores}", oreList.toString()));

        item.setAmount(remain);
        player.getInventory().setItemInMainHand(item);
    }

    @Subcommand("store all")
    @Description("Move items in your inventory into ore storage")
    @CommandPermission("oreprocessor.store.all")
    public void storeAll(Player player) {
        PlayerInventory inv = player.getInventory();
        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        Set<String> dirtyOres = new HashSet<>(1);
        int totalAdded = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack item = inv.getItem(i);
            if (ItemUtil.isEmpty(item))
                continue;
            UMaterial material = OreProcessor.getApi().identifyMaterial(item);
            if (material == null)
                continue;
            List<Ore> ores = OreProcessor.getApi().getStorageAllowItem(material);
            if (ores == null || ores.isEmpty())
                continue;
            int remain = item.getAmount();

            for (Ore ore : ores) {
                OreData oreData = playerData.requireOreData(ore.getId());

                int added = oreData.addProduct(material, remain, false);
                if (added == 0)
                    continue;

                totalAdded += added;
                remain -= added;
                dirtyOres.add(ore.getName());
                plugin.pluginLogger.scope("cmd/store/all")
                        .add("player", player)
                        .add("item", item.getType())
                        .add("ore", ore)
                        .add("expectedDelta", remain)
                        .add("actualDelta", added)
                        .add("remain", remain)
                        .add("totalAdded", added)
                        .flush();

                if (remain <= 0)
                    break;
            }

            item.setAmount(remain);
            inv.setItem(i, item);
        }

        if (dirtyOres.isEmpty()) {
            plugin.msg(player, plugin.messageConfig.cannotStoreItem);
            return;
        }

        plugin.msg(player, plugin.messageConfig.storedItems
                .replace("{amount}", Integer.toString(totalAdded))
                .replace("{ores}", String.join(", ", dirtyOres)));
    }
}
