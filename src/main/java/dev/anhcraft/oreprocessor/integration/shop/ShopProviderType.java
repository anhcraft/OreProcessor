package dev.anhcraft.oreprocessor.integration.shop;

public enum ShopProviderType {
    SHOPGUIPLUS("ShopGUIPlus");

    private final String plugin;

    ShopProviderType(String plugin) {
        this.plugin = plugin;
    }

    public String getPlugin() {
        return plugin;
    }
}
