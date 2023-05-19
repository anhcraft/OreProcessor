package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.palette.ui.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiRegistry {
    public static MenuGui MENU;
    public static UpgradeGui UPGRADE;
    public static StorageGui STORAGE;

    public static void openMenuGui(Player player) {
        MENU.open(player, new MenuGuiHandler());
    }

    public static void openUpgradeGui(Player player, Material product) {
        UPGRADE.open(player, new UpgradeGuiHandler(product));
    }

    public static void openStorageGui(Player player, Material product) {
        STORAGE.open(player, new StorageGuiHandler(product));
    }
}
