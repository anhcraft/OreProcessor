package dev.anhcraft.oreprocessor.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandHelp;
import co.aikar.commands.annotation.*;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.configdoc.ConfigDocGenerator;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.api.data.stats.TimeSeries;
import dev.anhcraft.oreprocessor.config.*;
import dev.anhcraft.oreprocessor.gui.*;
import dev.anhcraft.oreprocessor.integration.EventDebugger;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredListener;

import java.io.File;
import java.util.*;

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

    @Subcommand("upgrade throughput set")
    @CommandPermission("oreprocessor.upgrade.throughput.set")
    @CommandCompletion("@players @ores")
    @Description("Set throughput upgrade")
    public void setThroughputUpgrade(CommandSender sender, OfflinePlayer player, String ore, int amount) {
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

    @Subcommand("reload")
    @CommandPermission("oreprocessor.reload")
    @Description("Reload the plugin")
    public void reload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "OreProcessor reloaded!");
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
            int remain = playerData.requireOreData(ore).addProduct(material, amount, force);
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
            int actual = playerData.requireOreData(ore).takeProduct(material, amount);
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

    @Subcommand("store hand")
    @Description("Add the item in your main hand into ore storage")
    @CommandPermission("oreprocessor.store.hand")
    public void storeHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (ItemUtil.isEmpty(item)) {
            plugin.msg(player, plugin.messageConfig.emptyHand);
            return;
        }

        if (item.hasItemMeta()) { // prevent non-vanilla items
            plugin.msg(player, plugin.messageConfig.storeInvalidItem);
            return;
        }

        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        List<String> dirtyOres = new ArrayList<>(1);

        int remain = item.getAmount();

        for (String oreId : OreProcessor.getApi().getOres()) {
            Ore ore = OreProcessor.getApi().getOre(oreId);
            if (ore == null) continue;

            if (!ore.getAllowedProducts().contains(item.getType()) && !ore.getBestTransform(player).hasProduct(item.getType()))
                continue;

            OreData oreData = playerData.requireOreData(oreId);
            remain -= oreData.addProduct(item.getType(), remain, false);
            dirtyOres.add(ore.getName());
            if (remain <= 0) break;
        }

        if (dirtyOres.isEmpty()) {
            plugin.msg(player, plugin.messageConfig.cannotStoreItem);
            return;
        }

        plugin.msg(player, plugin.messageConfig.storedItems
                .replace("{amount}", Integer.toString(item.getAmount() - remain))
                .replace("{ores}", String.join(", ", dirtyOres)));

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
            if (ItemUtil.isEmpty(item) || item.hasItemMeta())
                continue;

            int remain = item.getAmount();

            for (String oreId : OreProcessor.getApi().getOres()) {
                Ore ore = OreProcessor.getApi().getOre(oreId);
                if (ore == null) continue;

                if (!ore.getAllowedProducts().contains(item.getType()) && !ore.getBestTransform(player).hasProduct(item.getType()))
                    continue;

                OreData oreData = playerData.requireOreData(oreId);
                int added = oreData.addProduct(item.getType(), remain, false);
                totalAdded += added;
                remain -= added;
                dirtyOres.add(ore.getName());
                if (remain <= 0) break;
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
