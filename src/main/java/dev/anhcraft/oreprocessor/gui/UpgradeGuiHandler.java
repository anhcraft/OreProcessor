package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.config.UpgradeLevel;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

import static dev.anhcraft.oreprocessor.gui.GuiRegistry.UPGRADE;

public class UpgradeGuiHandler extends GuiHandler {
    private final Material product;

    public UpgradeGuiHandler(Material product) {
        this.product = product;
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        listen("throughput", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {

            }
        });

        listen("capacity", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {

            }
        });

        listen("back", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openMenuGui(player);
            }
        });

        refresh(player);
    }

    public void refresh(Player player) {
        PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);

        int currentThroughput = playerData.getThroughput(product);
        UpgradeLevel nextThroughput = OreProcessor.getInstance().processingPlant.findNextThroughputUpgrade(currentThroughput);

        if (nextThroughput == null) {
            for (int slot : locateComponent("throughput")) {
                getSlot(slot).setEvents();

                replaceItem(slot, new ItemReplacer() {
                    @Override
                    public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                        itemBuilder.lore().addAll(UPGRADE.throughputMaximizedLore);
                        itemBuilder.replaceDisplay(new UnaryOperator<String>() {
                            @Override
                            public String apply(String s) {
                                return s.replace("{ore}", OreProcessor.getInstance().mainConfig.ores.get(product).name)
                                        .replace("{current-throughput}", Integer.toString(currentThroughput));
                            }
                        });
                        return itemBuilder;
                    }
                });
            }
        } else {
            boolean canUpgradeThroughput = OreProcessor.getInstance().economy.getBalance(player) >= nextThroughput.cost;

            for (int slot : locateComponent("throughput")) {
                if (canUpgradeThroughput)
                    getSlot(slot).setEvents((ClickEvent) (clickEvent, player1, slot1) -> upgradeThroughput(player1, nextThroughput));
                else
                    getSlot(slot).setEvents();

                replaceItem(slot, new ItemReplacer() {
                    @Override
                    public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                        itemBuilder.lore().addAll(canUpgradeThroughput ? UPGRADE.throughputUpgradableLore : UPGRADE.throughputUnaffordableLore);
                        itemBuilder.replaceDisplay(new UnaryOperator<String>() {
                            @Override
                            public String apply(String s) {
                                return s.replace("{ore}", OreProcessor.getInstance().mainConfig.ores.get(product).name)
                                        .replace("{current-throughput}", Integer.toString(currentThroughput))
                                        .replace("{next-throughput}", Integer.toString(nextThroughput.amount))
                                        .replace("{cost}", String.format("%.2f", nextThroughput.cost));
                            }
                        });
                        return itemBuilder;
                    }
                });
            }
        }

        int currentCapacity = playerData.getCapacity(product);
        UpgradeLevel nextCapacity = OreProcessor.getInstance().processingPlant.findNextCapacityUpgrade(currentCapacity);

        if (nextCapacity == null) {
            for (int slot : locateComponent("capacity")) {
                getSlot(slot).setEvents();

                replaceItem(slot, new ItemReplacer() {
                    @Override
                    public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                        itemBuilder.lore().addAll(UPGRADE.capacityMaximizedLore);
                        itemBuilder.replaceDisplay(new UnaryOperator<String>() {
                            @Override
                            public String apply(String s) {
                                return s.replace("{ore}", OreProcessor.getInstance().mainConfig.ores.get(product).name)
                                        .replace("{current-capacity}", Integer.toString(currentCapacity));
                            }
                        });
                        return itemBuilder;
                    }
                });
            }
        } else {
            boolean canUpgradeCapacity = OreProcessor.getInstance().economy.getBalance(player) >= nextCapacity.cost;

            for (int slot : locateComponent("capacity")) {
                if (canUpgradeCapacity)
                    getSlot(slot).setEvents((ClickEvent) (clickEvent, player1, slot1) -> upgradeCapacity(player1, nextCapacity));
                else
                    getSlot(slot).setEvents();

                replaceItem(slot, new ItemReplacer() {
                    @Override
                    public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                        itemBuilder.lore().addAll(canUpgradeCapacity ? UPGRADE.capacityUpgradableLore : UPGRADE.capacityUnaffordableLore);
                        itemBuilder.replaceDisplay(new UnaryOperator<String>() {
                            @Override
                            public String apply(String s) {
                                return s.replace("{ore}", OreProcessor.getInstance().mainConfig.ores.get(product).name)
                                        .replace("{current-capacity}", Integer.toString(currentCapacity))
                                        .replace("{next-capacity}", Integer.toString(nextCapacity.amount))
                                        .replace("{cost}", String.format("%.2f", nextCapacity.cost));
                            }
                        });
                        return itemBuilder;
                    }
                });
            }
        }
    }

    private void upgradeThroughput(Player player, UpgradeLevel nextThroughput) {
        if (OreProcessor.getInstance().economy.withdrawPlayer(player, nextThroughput.cost).transactionSuccess()) {
            PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
            playerData.setThroughput(product, nextThroughput.amount);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }
        refresh(player);
    }

    private void upgradeCapacity(Player player, UpgradeLevel nextCapacity) {
        if (OreProcessor.getInstance().economy.withdrawPlayer(player, nextCapacity.cost).transactionSuccess()) {
            PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
            playerData.setCapacity(product, nextCapacity.amount);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
        }
        refresh(player);
    }
}
