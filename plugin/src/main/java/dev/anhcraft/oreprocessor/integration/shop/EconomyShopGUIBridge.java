package dev.anhcraft.oreprocessor.integration.shop;

import dev.anhcraft.oreprocessor.api.ApiProvider;
import dev.anhcraft.oreprocessor.api.util.UMaterial;
import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.EconomyType;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("ConstantValue")
public class EconomyShopGUIBridge implements ShopProvider {
    @Override
    public boolean canSell(UMaterial material) {
        ItemStack i = ApiProvider.getApi().buildItem(material);
        if (i == null) return false;
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(i);
        return shopItem != null && !shopItem.isHidden() && shopItem.getEcoType().getType() == EconomyType.VAULT;
    }

    @Override
    public double getSellPrice(UMaterial material, int amount) {
        ItemStack i = ApiProvider.getApi().buildItem(material, amount);
        if (i == null) return 0;
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(i);
        Double sellPrice = EconomyShopGUIHook.getItemSellPrice(shopItem, i);
        return sellPrice == null ? 0 : sellPrice;
    }
}
