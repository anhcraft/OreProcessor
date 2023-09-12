package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.configdoc.ConfigDocGenerator;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.*;
import dev.anhcraft.oreprocessor.gui.*;
import dev.anhcraft.oreprocessor.integration.EventDebugger;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.util.Map;

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
    @Description("Open the menu")
    @CommandCompletion("@players")
    public void openMenu(Player player) {
        GuiRegistry.openMenuGui(player);
    }

    @Subcommand("inspect")
    @CommandPermission("oreprocessor.inspect")
    @Description("Inspect player data")
    public void inspect(Player inspector, OfflinePlayer reference) {
        if (!reference.hasPlayedBefore()) {
            inspector.sendMessage(ChatColor.RED + "This player has not played before!");
            return;
        }
        if (!reference.isOnline())
            inspector.sendMessage(ChatColor.YELLOW + "Fetching player data as he is currently offline...");

        OreProcessor.getApi().requirePlayerData(reference.getUniqueId()).whenComplete((playerData, throwable) -> {
            if (throwable != null) {
                inspector.sendMessage(ChatColor.RED + throwable.getMessage());
                return;
            }
            GuiRegistry.openInspectGui(inspector, playerData, reference.getName());
        });
    }

    @Subcommand("docs")
    @CommandPermission("oreprocessor.docs")
    @Description("Generate offline config documentation")
    public void generateDoc(CommandSender sender) {
        new ConfigDocGenerator()
                .withSchemaOf(MainConfig.class)
                .withSchemaOf(OreConfig.class)
                .withSchemaOf(PurgeStatsSettings.class)
                .withSchemaOf(AccessibilitySettings.class)
                .withSchemaOf(BehaviourConfig.class)
                .withSchemaOf(UpgradeConfig.class)
                .withSchemaOf(UpgradeLevelConfig.class)
                .withSchemaOf(MessageConfig.class)
                .withSchemaOf(MenuGui.class)
                .withSchemaOf(StorageGui.class)
                .withSchemaOf(UpgradeGui.class)
                .withSchemaOf(CraftingGui.class)
                .withSchemaOf(ItemBuilder.class)
                .generate(new File(plugin.getDataFolder(), "docs"));
        sender.sendMessage(ChatColor.GREEN + "Configuration documentation generated in plugins/OreProcessor/docs");
    }

    @Subcommand("reload")
    @CommandPermission("oreprocessor.reload")
    @Description("Reload the plugin")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "OreProcessor reloaded!");
    }

    @Subcommand("debugevents")
    @CommandPermission("oreprocessor.debugevents")
    @Description("Debug events")
    public void debugevents(CommandSender sender) {
        for (RegisteredListener listener : BlockBreakEvent.getHandlerList().getRegisteredListeners()) {
            sender.sendMessage(ChatColor.GREEN + String.format(
                    "BlockBreakEvent: %s from %s priority=%s",
                    listener.getListener().getClass().getName(),
                    listener.getPlugin().getName(),
                    listener.getPriority().name()
            ));
        }

        for (RegisteredListener listener : BlockDropItemEvent.getHandlerList().getRegisteredListeners()) {
            sender.sendMessage(ChatColor.GOLD + String.format(
                    "BlockDropItemEvent: %s from %s priority=%s",
                    listener.getListener().getClass().getName(),
                    listener.getPlugin().getName(),
                    listener.getPriority().name()
            ));
        }

        plugin.integrationManager.streamIntegration()
                .filter(integration -> integration instanceof EventDebugger)
                .forEach(integration -> {
            for (Map.Entry<String, HandlerList> entry : ((EventDebugger) integration).getEventHandlers().entrySet()) {
                for (RegisteredListener listener : entry.getValue().getRegisteredListeners()) {
                    sender.sendMessage(ChatColor.GOLD + String.format(
                            "%s: %s from %s priority=%s",
                            entry.getKey(),
                            listener.getListener().getClass().getName(),
                            listener.getPlugin().getName(),
                            listener.getPriority().name()
                    ));
                }
            }
        });
    }
}
