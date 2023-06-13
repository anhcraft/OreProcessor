package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.palette.ui.GuiHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;

public class GuiRefreshTask extends BukkitRunnable {
    @Override
    public void run() {
        // TODO Optimize this? e.g: use subscription model
        for (Player p : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = p.getOpenInventory().getTopInventory().getHolder();
            if (holder instanceof GuiHandler && holder instanceof AutoRefresh) {
                ((AutoRefresh) holder).refresh(p);
            }
        }
    }
}
