package dev.anhcraft.oreprocessor.api.integration;

public enum ShopProviderType {
    SHOPGUIPLUS("ShopGUIPlus"),
    ECONOMYSHOPGUI("EconomyShopGUI");

    private final String plugin;

    ShopProviderType(String plugin) {
        this.plugin = plugin;
    }

    public String getPlugin() {
        return plugin;
    }
}
