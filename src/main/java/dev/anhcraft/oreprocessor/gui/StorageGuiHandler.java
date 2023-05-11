package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import dev.anhcraft.palette.event.Action;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.event.PostPlaceEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import dev.anhcraft.palette.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StorageGuiHandler extends GuiHandler implements AutoRefresh {
    private final Material product;

    public StorageGuiHandler(Material product) {
        this.product = product;
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        visitComponent("input", slot -> {
            slot.makeModifiable().filter(i -> i.getType() == product);
            slot.listen(new PostPlaceEvent() {
                @Override
                public void onPostPlace(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item) {
                    PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
                    int stored = playerData.storeOre(product, item.getAmount());
                    int remain = item.getAmount() - stored;
                    if (remain == 0) resetBulk("input");
                    else {
                        ItemStack clone = item.clone();
                        clone.setAmount(remain);
                        setBulk("input", clone);
                    }
                }
            });
        });

        listen("output", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                if (ItemUtil.isPresent(player.getItemOnCursor())) return;
                PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
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
                int amount = playerData.takeOre(product, many);
                player.setItemOnCursor(new ItemStack(product, amount));
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1f, 1f);
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
        PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
        replaceItem("ore", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.material(product);
                itemBuilder.replaceDisplay(s -> s.replace("{queue}", Integer.toString(playerData.countQueuedOre(product)))
                        .replace("{storage-current}", Integer.toString(playerData.countStorage(product)))
                        .replace("{storage-capacity}", Integer.toString(playerData.getCapacity(product)))
                        .replace("{throughput}", Integer.toString(playerData.getThroughput(product))));
                return itemBuilder;
            }
        });
        replaceItem("input", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", OreProcessor.getInstance().mainConfig.ores.get(product).name)
                        .replace("{current}", Integer.toString(playerData.countStorage(product)))
                        .replace("{capacity}", Integer.toString(playerData.getCapacity(product))));
                return itemBuilder;
            }
        });
        replaceItem("output", new ItemReplacer() {
            @Override
            public @NotNull ItemBuilder apply(int slot, @NotNull ItemBuilder itemBuilder) {
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", OreProcessor.getInstance().mainConfig.ores.get(product).name)
                        .replace("{current}", Integer.toString(playerData.countStorage(product)))
                        .replace("{capacity}", Integer.toString(playerData.getCapacity(product))));
                return itemBuilder;
            }
        });
    }
}
