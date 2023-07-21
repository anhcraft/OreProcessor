package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.util.CraftingRecipe;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CraftingGuiHandler extends GuiHandler implements AutoRefresh {
    private final OreProcessor plugin;
    private final String oreId;
    private final Ore ore;
    private OreData oreData;

    public CraftingGuiHandler(String oreId) {
        this.plugin = OreProcessor.getInstance();
        this.oreId = oreId;
        this.ore = OreProcessor.getApi().requireOre(oreId);
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        PlayerData playerData = OreProcessor.getApi().getPlayerData(player);
        this.oreData = playerData.requireOreData(oreId);

        listen("ore", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openUpgradeGui(player, oreId);
            }
        });

        listen("back", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openStorageGui(player, oreId);
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
        Collections.sort(productSlots);
        List<Material> products = new ArrayList<>(oreData.getProducts());
        if (products.size() > productSlots.size()) {
            plugin.getLogger().warning(String.format(
                    "%s has %d products while GUI's display capability is %d (ore=%s)",
                    player.getName(), products.size(), productSlots.size(), ore.getName()
            ));
        }

        for (int i = 0; i < productSlots.size(); i++) {
            int slot = productSlots.get(i);

            if (i >= products.size()) {
                resetItem(slot);
                getSlot(slot).setEvents();
                continue;
            }

            Material product = products.get(i);
            CraftingRecipe recipe = GuiRegistry.CRAFTING.getRecipeFor(product);
            int productCount = oreData.countProduct(product);

            if (recipe == null || productCount < recipe.getInput().getAmount()) {
                ItemBuilder itemBuilder = GuiRegistry.CRAFTING.getUncraftableProductIcon();
                itemBuilder.material(product);
                itemBuilder.replaceDisplay(s -> s.replace("{current}", Integer.toString(productCount)));
                getInventory().setItem(slot, itemBuilder.build());
                getSlot(slot).setEvents();
                continue;
            }

            ItemBuilder itemBuilder = GuiRegistry.CRAFTING.getCraftableProductIcon();
            itemBuilder.material(product);
            itemBuilder.replaceDisplay(s -> s.replace("{current}", Integer.toString(productCount)));
            getInventory().setItem(slot, itemBuilder.build());
            getSlot(slot).setEvents(new ClickEvent() {
                @Override
                public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int i) {
                    // CRAFT
                    if (ItemUtil.isEmpty(player.getItemOnCursor())) {
                        Integer many = plugin.mainConfig.accessibilitySettings.craftAmount.get(clickEvent.getClick());
                        if (many == null || many <= 0) return;

                        oreData.testAndSetProduct(product, availableIngredients -> {
                            if (availableIngredients == 0) {
                                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                                return -1;
                            }
                            int expectedInput = recipe.getInput().getAmount() * many;
                            int actualInput = Math.min(expectedInput, availableIngredients);
                            int actualProduct = (int) Math.floor((double) actualInput / recipe.getInput().getAmount());
                            if (actualProduct == 0) {
                                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                                return -1;
                            }
                            oreData.addProduct(recipe.getOutput().getType(), actualProduct * recipe.getOutput().getAmount(), true);
                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                            int remainingInput = actualInput % recipe.getInput().getAmount();
                            return availableIngredients - actualInput + remainingInput;
                        });
                    } else {
                        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                    }
                }
            });
        }

    }
}
