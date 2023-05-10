package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import dev.anhcraft.palette.event.Action;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.event.PostPlaceEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import org.bukkit.Material;
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
            slot.makeModifiable()
                    .filter(i -> OreProcessor.getInstance().processingPlant.rawToProductMap.get(i.getType()) == product)
                    .disallowTaking();
            slot.listen(new PostPlaceEvent() {
                @Override
                public void onPostPlace(@NotNull Action action, @NotNull Player player, int slot, @NotNull ItemStack item) {
                    PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
                    playerData.queueOre();
                }
            });
        });

        listen("ore", new ClickEvent() {
            @Override
            public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                GuiRegistry.openUpgradeGui(player, product);
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
