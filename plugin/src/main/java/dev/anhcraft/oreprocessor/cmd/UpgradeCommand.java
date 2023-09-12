package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.anhcraft.oreprocessor.OreProcessor;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@CommandAlias("ore|oreprocessor")
public class UpgradeCommand extends BaseCommand {
    private final OreProcessor plugin;

    public UpgradeCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    private void preCheck(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive! (or zero to reset)");
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
    }

    @Subcommand("upgrade throughput set")
    @CommandPermission("oreprocessor.upgrade.throughput.set")
    @CommandCompletion("@players @ores")
    @Description("Set throughput upgrade")
    public void setThroughputUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        preCheck(sender, player, ore, amount);
        if (amount == 0)
            amount = OreProcessor.getApi().getDefaultThroughput();

        int finalAmount = amount;
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            if (Objects.equals(ore, "*")) {
                for (String oreId : playerData.listOreIds()) {
                    playerData.requireOreData(oreId).setThroughput(finalAmount);
                }
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's all ore throughput to %d (%d/m)",
                        player.getName(), finalAmount, OreProcessor.getApi().getThroughputPerMinute(finalAmount)
                ));
            } else {
                playerData.requireOreData(ore).setThroughput(finalAmount);
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's %s throughput to %d (%d/m)",
                        player.getName(), ore, finalAmount, OreProcessor.getApi().getThroughputPerMinute(finalAmount)
                ));
            }
        });
    }

    @Subcommand("upgrade capacity set")
    @CommandPermission("oreprocessor.upgrade.capacity.set")
    @CommandCompletion("@players @ores")
    @Description("Set capacity upgrade")
    public void setCapacityUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        preCheck(sender, player, ore, amount);
        if (amount == 0)
            amount = OreProcessor.getApi().getDefaultCapacity();

        int finalAmount = amount;
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            if (Objects.equals(ore, "*")) {
                for (String oreId : playerData.listOreIds()) {
                    playerData.requireOreData(oreId).setCapacity(finalAmount);
                }
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's all ores capacity to %d",
                        player.getName(), finalAmount
                ));
            } else {
                playerData.requireOreData(ore).setCapacity(finalAmount);
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Set %s's %s capacity to %d",
                        player.getName(), ore, finalAmount
                ));
            }
        });
    }

    @Subcommand("upgrade throughput add")
    @CommandPermission("oreprocessor.upgrade.throughput.add")
    @CommandCompletion("@players @ores")
    @Description("Add throughput upgrade")
    public void addThroughputUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        preCheck(sender, player, ore, amount);
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            if (Objects.equals(ore, "*")) {
                for (String oreId : playerData.listOreIds()) {
                    playerData.requireOreData(oreId).addThroughput(amount);
                }
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Add %s's all ore throughput by %d (%d/m)",
                        player.getName(), amount, OreProcessor.getApi().getThroughputPerMinute(amount)
                ));
            } else {
                playerData.requireOreData(ore).addThroughput(amount);
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Add %s's %s throughput by %d (%d/m)",
                        player.getName(), ore, amount, OreProcessor.getApi().getThroughputPerMinute(amount)
                ));
            }
        });
    }

    @Subcommand("upgrade capacity add")
    @CommandPermission("oreprocessor.upgrade.capacity.add")
    @CommandCompletion("@players @ores")
    @Description("Add capacity upgrade")
    public void addCapacityUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        preCheck(sender, player, ore, amount);
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            if (Objects.equals(ore, "*")) {
                for (String oreId : playerData.listOreIds()) {
                    playerData.requireOreData(oreId).addCapacity(amount);
                }
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Add %s's all ore capacity by %d",
                        player.getName(), amount
                ));
            } else {
                playerData.requireOreData(ore).addCapacity(amount);
                sender.sendMessage(ChatColor.GREEN + String.format(
                        "Add %s's %s capacity by %d",
                        player.getName(), ore, amount
                ));
            }
        });
    }
}
