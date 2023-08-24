package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.oreprocessor.api.data.PlayerData;
import org.bukkit.entity.Player;

public class GuiRegistry {
    public static MenuGui MENU;
    public static UpgradeGui UPGRADE;
    public static StorageGui STORAGE;
    public static CraftingGui CRAFTING;
    public static InspectGui INSPECT;

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

    public static void openInspectGui(Player inspector, PlayerData reference, String referenceName) {
        INSPECT.open(inspector, new InspectGuiHandler(reference), s -> s.replace("{reference}", referenceName));
    }
}
