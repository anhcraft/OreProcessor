package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.gui.GuiRegistry;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    @Subcommand("acquire")
    @CommandPermission("martialart.acquire")
    @CommandCompletion("@skills @players")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "OreProcessor reloaded!");
    }
}
