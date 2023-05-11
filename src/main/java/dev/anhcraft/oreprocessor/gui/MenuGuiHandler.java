package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.storage.PlayerData;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuGuiHandler extends GuiHandler implements AutoRefresh {

    @Override
    public void onPreOpen(@NotNull Player player) {
        refresh(player);
    }

    @Override
    public void refresh(Player player) {
        PlayerData playerData = OreProcessor.getInstance().playerDataManager.getData(player);
        List<Integer> slots = new ArrayList<>(locateComponent("ore"));
        Collections.sort(slots);
        Material[] ores = OreProcessor.getInstance().mainConfig.ores.keySet().toArray(new Material[0]);

        for (int i = 0; i < Math.min(slots.size(), ores.length); i++) {
            int slot = slots.get(i);
            Material ore = ores[i];

            replaceItem(slot, new ItemReplacer() {
                @Override
                public @NotNull ItemBuilder apply(int i, @NotNull ItemBuilder itemBuilder) {
                    itemBuilder.material(ore);
                    itemBuilder.name(GuiRegistry.MENU.oreName);
                    itemBuilder.lore(GuiRegistry.MENU.oreLore);
                    String oreName = OreProcessor.getInstance().mainConfig.ores.get(ore).name;
                    int queued = playerData.countQueuedOre(ore);
                    int stored = playerData.countStorage(ore);
                    int cap = playerData.getCapacity(ore);
                    String throughput = playerData.getThroughputPerMinute(ore);
                    itemBuilder.replaceDisplay(s -> s.replace("{ore}", oreName)
                            .replace("{queue}", Integer.toString(queued))
                            .replace("{storage-current}", Integer.toString(stored))
                            .replace("{storage-capacity}", Integer.toString(cap))
                            .replace("{storage-ratio}", Integer.toString((int) (((double) stored) / cap * 100d)))
                            .replace("{throughput}", throughput));
                    return itemBuilder;
                }
            });

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
