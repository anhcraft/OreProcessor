package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.OreData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@CommandAlias("ore|oreprocessor")
public class ModifyCommand extends BaseCommand {
    private final OreProcessor plugin;

    public ModifyCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Subcommand("add")
    @CommandPermission("oreprocessor.add")
    @Description("Add item to an ore storage")
    @CommandCompletion("@players @ores @materials")
    public void addOre(CommandSender sender, OfflinePlayer player, String ore, Material material, int amount, @Default("false") boolean force) {
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!Objects.equals(ore, "*") && OreProcessor.getApi().getOre(ore) == null) {
            sender.sendMessage(ChatColor.RED + "This ore does not exist!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            OreData oreData = playerData.requireOreData(ore);
            int oldTotalAmount = oreData.countProduct(material);
            int remain = oreData.addProduct(material, amount, force);
            plugin.pluginLogger.scope("cmd/add")
                    .add("sender", sender)
                    .add("target", player)
                    .add("ore", ore)
                    .add("material", material)
                    .add("amount", amount)
                    .add("force", force)
                    .add("remain", remain)
                    .add("oldTotalAmount", oldTotalAmount)
                    .add("newTotalAmount", oldTotalAmount + amount - remain)
                    .flush();
            if (force) {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Forced adding %d %s to %s's %s storage",
                        amount, material, player.getName(), ore
                ));
            } else {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Added %d %s to %s's %s storage (actual: %d)",
                        amount, material, player.getName(), ore, remain
                ));
            }
        });
    }

    @Subcommand("subtract")
    @CommandPermission("oreprocessor.subtract")
    @Description("Subtract item from an ore storage")
    @CommandCompletion("@players @ores @materials")
    public void subtractOre(CommandSender sender, OfflinePlayer player, String ore, Material material, int amount) {
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!Objects.equals(ore, "*") && OreProcessor.getApi().getOre(ore) == null) {
            sender.sendMessage(ChatColor.RED + "This ore does not exist!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            OreData oreData = playerData.requireOreData(ore);
            int oldTotalAmount = oreData.countProduct(material);
            int actual = oreData.takeProduct(material, amount);
            plugin.pluginLogger.scope("cmd/subtract")
                    .add("sender", sender)
                    .add("target", player)
                    .add("ore", ore)
                    .add("material", material)
                    .add("amount", amount)
                    .add("oldTotalAmount", oldTotalAmount)
                    .add("newTotalAmount", oldTotalAmount - actual)
                    .flush();
            if (actual == amount) {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Took %d %s from %s's %s storage",
                        amount, material, player.getName(), ore
                ));
            } else {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Tried taking %d %s to %s's %s storage (actual: %d)",
                        amount, material, player.getName(), ore, actual
                ));
            }
        });
    }
}
