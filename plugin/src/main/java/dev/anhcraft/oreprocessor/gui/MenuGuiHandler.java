package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.Ore;
import dev.anhcraft.oreprocessor.storage.data.PlayerDataConfig;
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

    @Override
    public void onPreOpen(@NotNull Player player) {
        PlayerDataConfig playerData = OreProcessor.getInstance().playerDataManager.getData(player);
        playerData.hideTutorial = true;
        playerData.markDirty();

        refresh(player);
    }

    @Override
    public void refresh(Player player) {
        PlayerDataConfig playerData = OreProcessor.getInstance().playerDataManager.getData(player);
        List<Integer> slots = new ArrayList<>(locateComponent("ore"));
        Collections.sort(slots);
        List<String> ores = OreProcessor.getApi().getOres();

        for (int i = 0; i < Math.min(slots.size(), ores.size()); i++) {
            int slot = slots.get(i);
            String oreId = ores.get(i);
            Ore ore = OreProcessor.getApi().requireOre(oreId);

            replaceItem(slot, new ItemReplacer() {
                @Override
                public @NotNull ItemBuilder apply(int i, @NotNull ItemBuilder itemBuilder) {
                    itemBuilder.material(ore.getIcon());
                    itemBuilder.name(GuiRegistry.MENU.oreName);
                    itemBuilder.lore(GuiRegistry.MENU.oreLore);
                    int queued = playerData.countQueuedOre(ore);
                    int stored = playerData.countStorage(ore);
                    int cap = playerData.getCapacity(ore);
                    String throughput = (getThroughput(ore) * 60d / OreProcessor.getInstance().mainConfig.processingSpeed);
                    itemBuilder.replaceDisplay(s -> s.replace("{ore}", oreName)
                            .replace("{processing}", Integer.toString(queued))
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
