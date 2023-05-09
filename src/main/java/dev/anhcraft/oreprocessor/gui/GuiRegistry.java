package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.palette.ui.Gui;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GuiRegistry {
    public static MenuGui MENU;
    public static UpgradeGui UPGRADE;
    public static Gui STORAGE;

    public static void openMenuGui(Player player) {
        MENU.open(player, new MenuGuiHandler());
    }

    public static void openUpgradeGui(Player player, Material ore) {
        UPGRADE.open(player, new UpgradeGuiHandler(ore));
    }

    public static void openStorageGui(Player player, Material ore) {
        STORAGE.open(player, new StorageGuiHandler(ore));
    }
}
