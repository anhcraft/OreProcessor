package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;

public class StorageGuiHandler extends GuiHandler implements AutoRefresh {
    private final static NumberFormat numberFormat = NumberFormat.getInstance();
    private final OreProcessor plugin;
    private final Material product;

    public StorageGuiHandler(Material product) {
        this.plugin = OreProcessor.getInstance();
        this.product = product;
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        listen("input", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull Player player, int i) {
                ItemStack cursor = player.getItemOnCursor();
                if (ItemUtil.isEmpty(cursor) || cursor.getType() != product) return;
                PlayerData playerData = plugin.playerDataManager.getData(player);
                int stored = playerData.storeOre(product, cursor.getAmount());
                int remain = cursor.getAmount() - stored;
                if (remain == 0) {
                    player.setItemOnCursor(null);
                } else {
                    ItemStack clone = cursor.clone();
                    clone.setAmount(remain);
                    player.setItemOnCursor(clone);
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1f, 1f);
            }
        });

        listen("output", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                int many;
                switch (clickEvent.getClick()) {
                    case LEFT:
                        many = 1;
                        break;
                    case RIGHT:
                        many = 64;
                        break;
                    case SHIFT_LEFT:
                        many = 16;
                        break;
                    case SHIFT_RIGHT:
                        many = 32;
                        break;
                    default:
                        return;
                }
                PlayerData playerData = plugin.playerDataManager.getData(player);
                int actual = playerData.takeOre(product, many);
                if (actual == 0) {
                    player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                    return;
                }
                ItemUtil.addToInventory(player, new ItemStack(product, actual));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1f, 1f);
            }
        });

        listen("quick-sell", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                plugin.integrationManager.getShopProvider(plugin.mainConfig.shopProvider)
                        .filter(sp -> sp.canSell(product))
                        .ifPresent(shopProvider -> {
                            int many;
                            switch (clickEvent.getClick()) {
                                case LEFT:
                                    many = Integer.MAX_VALUE;
                                    break;
                                case RIGHT:
                                    many = 64;
                                    break;
                                case SHIFT_LEFT:
                                    many = 16;
                                    break;
                                case SHIFT_RIGHT:
                                    many = 32;
                                    break;
                                default:
                                    return;
                            }
                            PlayerData playerData = plugin.playerDataManager.getData(player);
                            if (!playerData.testAndTakeOre(product, many, actual -> {
                                double profit = shopProvider.getSellPrice(product, actual);
                                boolean success = profit > 0 && plugin.economy.depositPlayer(player, profit).transactionSuccess();
                                if (success) {
                                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                    plugin.msg(player, plugin.messageConfig.quickSellSuccess
                                            .replace("{amount}", Integer.toString(actual))
                                            .replace("{ore}", product.name())
                                            .replace("{profit}", numberFormat.format(profit))
                                    );
                                } else {
                                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                                    plugin.msg(player, plugin.messageConfig.quickSellFailed);
                                }
                                return success;
                            })) {
                                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                            }
                        });
            }
        });

        listen("ore", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openUpgradeGui(player, product);
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

    @Override
    public void refresh(Player player) {
        PlayerData playerData = plugin.playerDataManager.getData(player);
        replaceItem("ore", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.material(product);
                String oreName = plugin.mainConfig.ores.get(product).name;
                int queued = playerData.countQueuedOre(product);
                int stored = playerData.countStorage(product);
                int cap = playerData.getCapacity(product);
                String throughput = playerData.getThroughputPerMinute(product);
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", oreName)
                        .replace("{processing}", Integer.toString(queued))
                        .replace("{storage-current}", Integer.toString(stored))
                        .replace("{storage-capacity}", Integer.toString(cap))
                        .replace("{storage-ratio}", Integer.toString((int) (((double) stored) / cap * 100d)))
                        .replace("{throughput}", throughput));
                return itemBuilder;
            }
        });
        replaceItem("input", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", plugin.mainConfig.ores.get(product).name)
                        .replace("{current}", Integer.toString(playerData.countStorage(product)))
                        .replace("{capacity}", Integer.toString(playerData.getCapacity(product))));
                return itemBuilder;
            }
        });
        replaceItem("output", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", plugin.mainConfig.ores.get(product).name)
                        .replace("{current}", Integer.toString(playerData.countStorage(product)))
                        .replace("{capacity}", Integer.toString(playerData.getCapacity(product))));
                return itemBuilder;
            }
        });
        replaceItem("quick-sell", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                plugin.integrationManager.getShopProvider(plugin.mainConfig.shopProvider)
                        .ifPresent(shopProvider -> {
                            if (shopProvider.canSell(product))
                                itemBuilder.lore(GuiRegistry.STORAGE.quickSellAvailableLore);
                            else
                                itemBuilder.lore(GuiRegistry.STORAGE.quickSellUnavailableLore);
                        });
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", plugin.mainConfig.ores.get(product).name)
                        .replace("{current}", Integer.toString(playerData.countStorage(product)))
                        .replace("{capacity}", Integer.toString(playerData.getCapacity(product))));
                return itemBuilder;
            }
        });
    }
}
