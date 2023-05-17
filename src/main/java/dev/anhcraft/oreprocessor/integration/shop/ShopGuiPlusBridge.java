package dev.anhcraft.oreprocessor.integration.shop;

import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ShopGuiPlusBridge implements ShopProvider {

    @Override
    public boolean canSell(Material material) {
        return ShopGuiPlusApi.getItemStackShopItem(new ItemStack(material, 1)) != null;
    }

    @Override
    public double getSellPrice(Material material, int amount) {
        return ShopGuiPlusApi.getItemStackShopItem(new ItemStack(material, 1)).getSellPriceForAmount(amount);
    }
}
