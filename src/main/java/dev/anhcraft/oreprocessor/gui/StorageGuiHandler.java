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

public class StorageGuiHandler extends GuiHandler implements AutoRefresh {
    private final Material product;

    public StorageGuiHandler(Material product) {
        this.product = product;
    }

    @Override
    public void onPreOpen(@NotNull Player player) {
        listen("input", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent inventoryClickEvent, @NotNull Player player, int i) {
                ItemStack cursor = player.getItemOnCursor();
                if (ItemUtil.isEmpty(cursor) || cursor.getType() != product) return;
                PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
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
                ItemStack cursor = player.getItemOnCursor();
                if (ItemUtil.isPresent(cursor) && cursor.getType() != product) return;
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
                PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
                if (ItemUtil.isPresent(cursor)) {
                    many = Math.min(many, product.getMaxStackSize() - cursor.getAmount());
                    player.setItemOnCursor(new ItemStack(product, cursor.getAmount() + playerData.takeOre(product, many)));
                } else {
                    player.setItemOnCursor(new ItemStack(product, playerData.takeOre(product, many)));
                }
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
                String oreName = OreProcessor.getInstance().mainConfig.ores.get(product).name;
                int queued = playerData.countQueuedOre(product);
                int stored = playerData.countStorage(product);
                int cap = playerData.getCapacity(product);
                String throughput = playerData.getThroughputPerMinute(product);
                itemBuilder.replaceDisplay(s -> s.replace("{ore}", oreName)
                        .replace("{queue}", Integer.toString(queued))
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
