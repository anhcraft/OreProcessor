package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.stats.TimeSeries;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

@CommandAlias("ore|oreprocessor")
public class StatsCommand extends BaseCommand {
    private final OreProcessor plugin;

    public StatsCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Subcommand("stats server")
    @CommandPermission("oreprocessor.stats.server")
    @Description("Display server stats")
    public void statsServerAll(CommandSender sender, String oreQuery) {
        displayStats(sender, oreQuery, "&cServer", TimeSeries.parseAll(OreProcessor.getApi().getServerData(), oreQuery));
    }

    @Subcommand("stats player")
    @CommandPermission("oreprocessor.stats.player")
    @Description("Display player stats")
    public void statsPlayerAll(CommandSender sender, OfflinePlayer player, String oreQuery) {
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            displayStats(sender, oreQuery, player.getName(), TimeSeries.parseAll(playerData, oreQuery));
        });
    }

    private void displayStats(CommandSender sender, String oreQuery, String target, TimeSeries timeSeries) {
        for (String s : plugin.messageConfig.statisticCumulativeDetails) {
            plugin.rawMsg(sender, s.replace("{ore-query}", oreQuery)
                    .replace("{target}", target)
                    .replace("{total-mined}", Long.toString(timeSeries.getMiningCount()))
                    .replace("{total-feedstock}", Long.toString(timeSeries.getFeedstockCount()))
                    .replace("{total-products}", Long.toString(timeSeries.getProductCount())));
        }
    }
}
