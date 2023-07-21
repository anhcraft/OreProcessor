package dev.anhcraft.oreprocessor.gui;

import org.bukkit.entity.Player;

public class GuiRegistry {
    public static MenuGui MENU;
    public static UpgradeGui UPGRADE;
    public static StorageGui STORAGE;
    public static CraftingGui CRAFTING;

    public static void openMenuGui(Player player) {
        MENU.open(player, new MenuGuiHandler());
    }

    public static void openUpgradeGui(Player player, String ore) {
        UPGRADE.open(player, new UpgradeGuiHandler(ore));
    }

    public static void openStorageGui(Player player, String ore) {
        STORAGE.open(player, new StorageGuiHandler(ore));
    }

    public static void openCraftGui(Player player, String ore) {
        CRAFTING.open(player, new CraftingGuiHandler(ore));
    }
}
