package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import dev.anhcraft.oreprocessor.api.upgrade.UpgradeLevel;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

import static dev.anhcraft.oreprocessor.gui.GuiRegistry.UPGRADE;

public class UpgradeGuiHandler extends GuiHandler {
    private final static NumberFormat numberFormat = NumberFormat.getInstance();
    private final String oreId;
    private final Ore ore;
    private final OreProcessor plugin;
    private IOreData oreData;

    public UpgradeGuiHandler(String oreId) {
        this.plugin = OreProcessor.getInstance();
        this.oreId = oreId;
        this.ore = OreProcessor.getApi().requireOre(oreId);
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        IPlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        this.oreData = playerData.requireOreData(oreId);

        listen("back", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openMenuGui(player);
            }
        });

        refresh(player);
    }

    public void refresh(Player player) {
        refreshThroughput(player);
        refreshCapacity(player);
    }

    private void refreshThroughput(Player player) {
        int currentThroughput = oreData.getThroughput();
        UpgradeLevel nextThroughput = OreProcessor.getApi().getNextThroughputUpgrade(currentThroughput);
        String currentThroughputM = Integer.toString(OreProcessor.getApi().getThroughputPerMinute(currentThroughput));

        if (nextThroughput == null) {
            ItemBuilder itemBuilder = UPGRADE.getThroughputMaximizedIcon();
            itemBuilder.replaceDisplay(s -> s.replace("{ore}", ore.getName())
                    .replace("{current-throughput}", currentThroughputM));

            for (int slot : locateComponent("throughput")) {
                getSlot(slot).setEvents();
                getInventory().setItem(slot, itemBuilder.build());
            }
        } else {
            boolean affordable = plugin.economy.getBalance(player) >= nextThroughput.getCost();

            ItemBuilder itemBuilder = affordable ? UPGRADE.getThroughputUpgradableIcon() : UPGRADE.getThroughputUnaffordableIcon();
            String nextThroughputM = Integer.toString(OreProcessor.getApi().getThroughputPerMinute(nextThroughput.getAmount()));
            itemBuilder.replaceDisplay(s -> s.replace("{ore}", ore.getName())
                    .replace("{current-throughput}", currentThroughputM)
                    .replace("{next-throughput}", nextThroughputM)
                    .replace("{cost}", numberFormat.format(nextThroughput.getCost())));

            if (affordable) {
                for (int slot : locateComponent("throughput")) {
                    getSlot(slot).setEvents((ClickEvent) (clickEvent, player1, slot1) -> upgradeThroughput(player1, nextThroughput));
                    getInventory().setItem(slot, itemBuilder.build());
                }
            } else {
                for (int slot : locateComponent("throughput")) {
                    getSlot(slot).setEvents();
                    getInventory().setItem(slot, itemBuilder.build());
                }
            }
        }
    }

    private void refreshCapacity(Player player) {
        int currentCapacity = oreData.getCapacity();
        UpgradeLevel nextCapacity = OreProcessor.getApi().getNextCapacityUpgrade(currentCapacity);

        if (nextCapacity == null) {
            ItemBuilder itemBuilder = UPGRADE.getCapacityMaximizedIcon();
            itemBuilder.replaceDisplay(s -> s.replace("{ore}", ore.getName())
                    .replace("{current-capacity}", Integer.toString(currentCapacity)));

            for (int slot : locateComponent("capacity")) {
                getSlot(slot).setEvents();
                getInventory().setItem(slot, itemBuilder.build());
            }
        } else {
            boolean affordable = plugin.economy.getBalance(player) >= nextCapacity.getCost();

            ItemBuilder itemBuilder = affordable ? UPGRADE.getCapacityUpgradableIcon() : UPGRADE.getCapacityUnaffordableIcon();
            itemBuilder.replaceDisplay(s -> s.replace("{ore}", ore.getName())
                    .replace("{current-capacity}", Integer.toString(currentCapacity))
                    .replace("{next-capacity}", Integer.toString(nextCapacity.getAmount()))
                    .replace("{cost}", numberFormat.format(nextCapacity.getCost())));

            if (affordable) {
                for (int slot : locateComponent("capacity")) {
                    getSlot(slot).setEvents((ClickEvent) (clickEvent, player1, slot1) -> upgradeCapacity(player1, nextCapacity));
                    getInventory().setItem(slot, itemBuilder.build());
                }
            } else {
                for (int slot : locateComponent("capacity")) {
                    getSlot(slot).setEvents();
                    getInventory().setItem(slot, itemBuilder.build());
                }
            }
        }
    }

    private void upgradeThroughput(Player player, UpgradeLevel nextThroughput) {
        if (plugin.economy.withdrawPlayer(player, nextThroughput.getCost()).transactionSuccess()) {
            oreData.setThroughput(nextThroughput.getAmount());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            plugin.msg(player, plugin.messageConfig.upgradeThroughputSuccess.replace("{ore}", ore.getName())
                    .replace("{amount}", Integer.toString(OreProcessor.getApi().getThroughputPerMinute(nextThroughput.getAmount()))));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            plugin.msg(player, plugin.messageConfig.upgradeThroughputFailed);
        }
        refresh(player);
    }

    private void upgradeCapacity(Player player, UpgradeLevel nextCapacity) {
        if (plugin.economy.withdrawPlayer(player, nextCapacity.getCost()).transactionSuccess()) {
            oreData.setCapacity(nextCapacity.getAmount());
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            plugin.msg(player, plugin.messageConfig.upgradeCapacitySuccess.replace("{ore}", ore.getName())
                    .replace("{amount}", Integer.toString(nextCapacity.getAmount())));
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            plugin.msg(player, plugin.messageConfig.upgradeCapacityFailed);
        }
        refresh(player);
    }
}
