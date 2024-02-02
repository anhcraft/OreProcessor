package dev.anhcraft.oreprocessor.integration.shop;

import dev.anhcraft.oreprocessor.api.ApiProvider;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.inventory.ItemStack;

public class ShopGuiPlusBridge implements ShopProvider {

    @Override
    public boolean canSell(UMaterial material) {
        ItemStack i = ApiProvider.getApi().buildItem(material);
        if (i == null) return false;
        return ShopGuiPlusApi.getItemStackShopItem(i) != null;
    }

    @Override
    public double getSellPrice(UMaterial material, int amount) {
        return ShopGuiPlusApi.getItemStackShopItem(ApiProvider.getApi().buildItem(material)).getSellPriceForAmount(amount);
    }
}
