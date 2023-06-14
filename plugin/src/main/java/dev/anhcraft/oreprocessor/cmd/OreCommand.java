package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.configdoc.ConfigDocGenerator;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.MainConfig;
import dev.anhcraft.oreprocessor.config.MessageConfig;
import dev.anhcraft.oreprocessor.config.OreConfig;
import dev.anhcraft.oreprocessor.config.UpgradeLevelConfig;
import dev.anhcraft.oreprocessor.gui.GuiRegistry;
import dev.anhcraft.oreprocessor.gui.MenuGui;
import dev.anhcraft.oreprocessor.gui.StorageGui;
import dev.anhcraft.oreprocessor.gui.UpgradeGui;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

@CommandAlias("ore|oreprocessor")
public class OreCommand extends BaseCommand {
    private final OreProcessor plugin;

    public OreCommand(OreProcessor plugin) {
        this.plugin = plugin;
    }

    @HelpCommand
    @CatchUnknown
    public void doHelp(CommandSender sender, CommandHelp help) {
        help.showHelp();
    }

    @Default
    public void openMenu(Player player) {
        GuiRegistry.openMenuGui(player);
    }

    @Subcommand("docs")
    @CommandPermission("oreprocessor.docs")
    public void generateDoc(CommandSender sender) {
        new ConfigDocGenerator()
                .withSchemaOf(MainConfig.class)
                .withSchemaOf(OreConfig.class)
                .withSchemaOf(UpgradeLevelConfig.class)
                .withSchemaOf(MessageConfig.class)
                .withSchemaOf(MenuGui.class)
                .withSchemaOf(StorageGui.class)
                .withSchemaOf(UpgradeGui.class)
                .withSchemaOf(ItemBuilder.class)
                .generate(new File(plugin.getDataFolder(), "docs"));
        sender.sendMessage(ChatColor.GREEN + "Configuration documentation generated in plugins/OreProcessor/docs");
    }

    @Subcommand("ore throughput set")
    @CommandPermission("oreprocessor.upgrade.throughput.set")
    @CommandCompletion("@players @ores")
    public void setThroughputUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive! (or zero to reset)");
            return;
        }
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (OreProcessor.getApi().getOre(ore) == null) {
            sender.sendMessage(ChatColor.RED + "This ore does not exist!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        if (amount == 0)
            amount = OreProcessor.getApi().getDefaultThroughput();

        int finalAmount = amount;
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            playerData.requireOreData(ore).setThroughput(finalAmount);
            sender.sendMessage(ChatColor.GREEN + String.format(
                    "Set %s's %s throughput to %d (%d/m)",
                    player.getName(), ore, finalAmount, OreProcessor.getApi().getThroughputPerMinute(finalAmount)
            ));
        });
    }

    @Subcommand("upgrade capacity set")
    @CommandPermission("oreprocessor.upgrade.capacity.set")
    @CommandCompletion("@players @ores")
    public void setCapacityUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "The amount must be positive! (or zero to reset)");
            return;
        }
        if (!player.hasPlayedBefore()) {
            sender.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (OreProcessor.getApi().getOre(ore) == null) {
            sender.sendMessage(ChatColor.RED + "This ore does not exist!");
            return;
        }
        if (!player.isOnline())
            sender.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        if (amount == 0)
            amount = OreProcessor.getApi().getDefaultCapacity();

        int finalAmount = amount;
        OreProcessor.getApi().requirePlayerData(player.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                sender.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            playerData.requireOreData(ore).setCapacity(finalAmount);
            sender.sendMessage(ChatColor.GREEN + String.format(
                    "Set %s's %s capacity to %d",
                    player.getName(), ore, finalAmount
            ));
        });
    }

    @Subcommand("reload")
    @CommandPermission("oreprocessor.reload")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "OreProcessor reloaded!");
    }
}
