package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.api.data.OreData;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.palette.event.ClickEvent;
import dev.anhcraft.palette.ui.GuiHandler;
import dev.anhcraft.palette.util.ItemReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuGuiHandler extends GuiHandler implements AutoRefresh {
    private PlayerData playerData;

    @Override
    public void onPreOpen(@NotNull Player player) {
        playerData = OreProcessor.getApi().getPlayerData(player);
        playerData.hideTutorial(true);

        refresh(player);
    }

    @Override
    public void refresh(Player player) {
        List<Integer> slots = new ArrayList<>(locateComponent("ore"));
        Collections.sort(slots);
        List<String> ores = new ArrayList<>(OreProcessor.getApi().getOres());

        for (int i = 0; i < slots.size(); i++) {
            int slot = slots.get(i);

            if (i >= ores.size()) {
                resetItem(slot);
                getSlot(slot).clearEvents();
                continue;
            }

            String oreId = ores.get(i);
            Ore ore = OreProcessor.getApi().requireOre(oreId);
            OreData oreData = playerData.requireOreData(oreId);

            replaceItem(slot, new ItemReplacer() {
                @Override
                public @NotNull ItemBuilder apply(int i, @NotNull ItemBuilder itemBuilder) {
                    itemBuilder.material(ore.getIcon());
                    itemBuilder.name(GuiRegistry.MENU.oreName);
                    itemBuilder.lore(GuiRegistry.MENU.oreLore);
                    int processing = oreData.countAllFeedstock();
                    int stored = oreData.countAllProducts();
                    int cap = oreData.getCapacity();
                    int throughputM = OreProcessor.getApi().getThroughputPerMinute(oreData.getThroughput());
                    itemBuilder.replaceDisplay(s -> s.replace("{ore}", ore.getName())
                            .replace("{processing}", Integer.toString(processing))
                            .replace("{storage-current}", Integer.toString(stored))
                            .replace("{storage-capacity}", Integer.toString(cap))
                            .replace("{storage-ratio}", Integer.toString((int) (((double) stored) / cap * 100d)))
                            .replace("{throughput}", Integer.toString(throughputM)));
                    return itemBuilder;
                }
            });

            getSlot(slot).setEvents(new ClickEvent() {
                @Override
                public void onClick(@NotNull InventoryClickEvent clickEvent, @NotNull Player player, int slot) {
                    if (clickEvent.isLeftClick()) {
                        GuiRegistry.openStorageGui(player, oreId);
                    } else if (clickEvent.isRightClick()) {
                        GuiRegistry.openUpgradeGui(player, oreId);
                    }
                }
            });
        }
    }
}
