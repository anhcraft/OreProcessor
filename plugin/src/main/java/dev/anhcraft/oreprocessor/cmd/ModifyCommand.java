package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@CommandAlias("ore|oreprocessor")
public class ModifyCommand extends BaseCommand {
    private final OreProcessor plugin;

    public ModifyCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @Subcommand("set")
    @CommandPermission("oreprocessor.set")
    @Description("Put item into an ore storage")
    @CommandCompletion("@players @ores @materials")
    public void setOre(CommandSender sender, OfflinePlayer player, String ore, String material, int amount, @Default("false") boolean force) {
        UMaterial uMaterial = UMaterial.parse(material);
        if (uMaterial == null) {
            sender.sendMessage(ChatColor.RED + "Invalid material!");
            return;
        }
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
            int oldTotalAmount = oreData.countProduct(uMaterial);
            int actualNewAmount = oreData.setProduct(uMaterial, amount, force);
            plugin.pluginLogger.scope("cmd/add")
                    .add("sender", sender)
                    .add("target", player)
                    .add("ore", ore)
                    .add("material", material)
                    .add("force", force)
                    .add("oldTotalAmount", oldTotalAmount)
                    .add("expectedNewAmount", amount)
                    .add("actualNewAmount", actualNewAmount)
                    .flush();
            if (force) {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Forced putting %d %s into %s's %s storage",
                        amount, material, player.getName(), ore
                ));
            } else {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Put %d %s into %s's %s storage (actual: %d)",
                        amount, material, player.getName(), ore, actualNewAmount
                ));
            }
        });
    }

    @Subcommand("add")
    @CommandPermission("oreprocessor.add")
    @Description("Add item to an ore storage")
    @CommandCompletion("@players @ores @materials")
    public void addOre(CommandSender sender, OfflinePlayer player, String ore, String material, int amount, @Default("false") boolean force) {
        UMaterial uMaterial = UMaterial.parse(material);
        if (uMaterial == null) {
            sender.sendMessage(ChatColor.RED + "Invalid material!");
            return;
        }
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
            int oldTotalAmount = oreData.countProduct(uMaterial);
            int actualDelta = oreData.addProduct(uMaterial, amount, force);
            plugin.pluginLogger.scope("cmd/add")
                    .add("sender", sender)
                    .add("target", player)
                    .add("ore", ore)
                    .add("material", material)
                    .add("force", force)
                    .add("expectedDelta", amount)
                    .add("actualDelta", actualDelta)
                    .add("oldTotalAmount", oldTotalAmount)
                    .add("newTotalAmount", oldTotalAmount + actualDelta)
                    .flush();
            if (force) {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Forced adding %d %s to %s's %s storage",
                        amount, material, player.getName(), ore
                ));
            } else {
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Added %d %s to %s's %s storage (actual: %d)",
                        amount, material, player.getName(), ore, actualDelta
                ));
            }
        });
    }

    @Subcommand("subtract")
    @CommandPermission("oreprocessor.subtract")
    @Description("Subtract item from an ore storage")
    @CommandCompletion("@players @ores @materials")
    public void subtractOre(CommandSender sender, OfflinePlayer player, String ore, String material, int amount) {
        UMaterial uMaterial = UMaterial.parse(material);
        if (uMaterial == null) {
            sender.sendMessage(ChatColor.RED + "Invalid material!");
            return;
        }
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
            int oldTotalAmount = oreData.countProduct(uMaterial);
            int actual = oreData.takeProduct(uMaterial, amount);
            plugin.pluginLogger.scope("cmd/subtract")
                    .add("sender", sender)
                    .add("target", player)
                    .add("ore", ore)
                    .add("material", material)
                    .add("expectedDelta", amount)
                    .add("actualDelta", actual)
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
