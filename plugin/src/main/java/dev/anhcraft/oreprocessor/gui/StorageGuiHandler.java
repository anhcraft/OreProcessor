package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.IOreData;
import dev.anhcraft.oreprocessor.api.data.IPlayerData;
import dev.anhcraft.oreprocessor.integration.shop.ShopProvider;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import dev.anhcraft.palette.util.ItemUtil;
import org.apache.commons.lang.mutable.MutableDouble;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageGuiHandler extends GuiHandler implements AutoRefresh {
    private final static NumberFormat numberFormat = NumberFormat.getInstance();
    private final OreProcessor plugin;
    private final String oreId;
    private final Ore ore;
    private IOreData oreData;

    public StorageGuiHandler(String oreId) {
        this.plugin = OreProcessor.getInstance();
        this.oreId = oreId;
        this.ore = OreProcessor.getApi().requireOre(oreId);
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        IPlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        this.oreData = playerData.requireOreData(oreId);

        listen("quick-sell", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                Optional<ShopProvider> shopProvider = plugin.integrationManager.getShopProvider(OreProcessor.getApi().getShopProvider());
                if (!shopProvider.isPresent()) return;

                double proportion;
                switch (clickEvent.getClick()) {
                    case LEFT:
                        proportion = 1;
                        break;
                    case RIGHT:
                        proportion = 0.5;
                        break;
                    case SHIFT_LEFT:
                        proportion = 0.25;
                        break;
                    case SHIFT_RIGHT:
                        proportion = 0.1;
                        break;
                    default:
                        return;
                }

                MutableDouble profits = new MutableDouble(0);
                MutableDouble count = new MutableDouble(0);
                for (Material product : oreData.getProducts()) {
                    if (!shopProvider.get().canSell(product)) continue;
                    int amount = (int) (oreData.countProduct(product) * proportion);
                    if (amount <= 0) continue;

                    oreData.testAndTakeProduct(product, amount, actual -> {
                        count.add(actual);
                        double profit = shopProvider.get().getSellPrice(product, actual);
                        boolean success = plugin.economy.depositPlayer(player, profit).transactionSuccess();
                        if (success) {
                            profits.add(profit);
                        } else {
                            plugin.getLogger().warning(String.format(
                                    "Failed to deposit %.3f to %s's account for selling %d %s",
                                    profit, player.getName(), actual, product.getKey()
                            ));
                        }
                        return success;
                    });
                }

                if (count.doubleValue() == 0) return;

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                plugin.msg(player, plugin.messageConfig.quickSellSuccess
                        .replace("{amount}", count.toString())
                        .replace("{profits}", numberFormat.format(profits.doubleValue()))
                );
            }
        });

        listen("ore", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openUpgradeGui(player, oreId);
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
        int stored = oreData.countAllProducts();
        int cap = oreData.getCapacity();
        int processing = oreData.countAllFeedstock();
        int throughputM = OreProcessor.getApi().getThroughputPerMinute(oreData.getThroughput());

        replaceItem("ore", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.material(ore.getIcon());
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", ore.getName())
                        .replace("{processing}", Integer.toString(processing))
                        .replace("{storage-current}", Integer.toString(stored))
                        .replace("{storage-capacity}", Integer.toString(cap))
                        .replace("{storage-ratio}", Integer.toString((int) (((double) stored) / cap * 100d)))
                        .replace("{throughput}", Integer.toString(throughputM)));
                return itemBuilder;
            }
        });

        List<Integer> productSlots = new ArrayList<>(locateComponent("product"));
        List<Material> products = new ArrayList<>(oreData.getProducts());
        if (products.size() > productSlots.size()) {
            plugin.getLogger().warning(String.format(
                    "%s has %d products while GUI's display capability is %d; ore: %s",
                    player.getName(), products.size(), productSlots.size(), ore.getName()
            ));
        }

        for (int i = 0; i < productSlots.size(); i++) {
            int slot = productSlots.get(i);

            if (i >= products.size()) {
                getSlot(slot).setEvents();
                continue;
            }

            Material product = products.get(i);
            ItemBuilder itemBuilder = GuiRegistry.STORAGE.getProductIcon();
            itemBuilder.material(product);

            itemBuilder.replaceDisplay(s -> s
                    .replace("{current}", Integer.toString(oreData.countProduct(product)))
                    .replace("{storage-current}", Integer.toString(stored))
                    .replace("{storage-capacity}", Integer.toString(cap))
            );

            getInventory().setItem(slot, itemBuilder.build());
            getSlot(slot).setEvents(new ClickEvent() {
                @Override
                public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int i) {
                    ItemStack cursor = player.getItemOnCursor();

                    // TAKE
                    if (ItemUtil.isEmpty(cursor)) {
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
                        int actual = oreData.takeProduct(product, many);
                        if (actual == 0) {
                            player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                            return;
                        }
                        ItemUtil.addToInventory(player, new ItemStack(product, actual));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1f, 1f);
                    }

                    // ADD
                    else if (cursor.getType() == product) {
                        int stored = oreData.addProduct(product, cursor.getAmount(), false);
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
                }
            });
        }

        resetBulk("quick-sell");

        if (player.hasPermission("oreprocessor.quick-sell")) {
            Optional<ShopProvider> shopProvider = plugin.integrationManager.getShopProvider(OreProcessor.getApi().getShopProvider());
            if (shopProvider.isPresent()) {
                ItemBuilder itemBuilder = GuiRegistry.STORAGE.getQuickSellIcon();
                itemBuilder.replaceDisplay(s -> s
                        .replace("{storage-current}", Integer.toString(stored))
                        .replace("{storage-capacity}", Integer.toString(cap)));
                setBulk("quick-sell", itemBuilder.build());
            }
        }
    }
}
