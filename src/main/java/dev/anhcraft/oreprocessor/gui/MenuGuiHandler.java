package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class MenuGuiHandler extends GuiHandler implements AutoRefresh {

    @Override
    public void onPreOpen(@NotNull Player player) {
        refresh(player);
    }

    @Override
    public void refresh(Player player) {
        PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
        List<Integer> slots = new ArrayList<>(locateComponent("ore"));

        Material[] ores = OreProcessor.getInstance().processingPlant.rawToProductMap.keySet().toArray(new Material[0]);
        for (int i = 0; i < Math.min(slots.size(), ores.length); i++) {
            int slot = slots.get(i);
            Material ore = ores[i];

            ItemBuilder itemBuilder = new ItemBuilder();
            itemBuilder.material(ore);
            itemBuilder.lore(new ArrayList<>(GuiRegistry.MENU.oreLore));
            itemBuilder.replaceDisplay(new UnaryOperator<String>() {
                @Override
                public String apply(String s) {
                    return s.replace("{queue}", Integer.toString(playerData.countQueuedOre(ore)))
                            .replace("{storage-current}", Integer.toString(playerData.countStoredOre(ore)))
                            .replace("{storage-capacity}", Integer.toString(playerData.getCapacity(ore)))
                            .replace("{throughput}", Integer.toString(playerData.getThroughput(ore)));
                }
            });
            getInventory().setItem(slot, itemBuilder.build());

            getSlot(slot).setEvents(new ClickEvent() {
                @Override
                public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                    if (clickEvent.isLeftClick()) {
                        GuiRegistry.openStorageGui(player, ore);
                    } else if (clickEvent.isRightClick()) {
                        GuiRegistry.openUpgradeGui(player, ore);
                    }
                }
            });
        }
    }
}
