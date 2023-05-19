package dev.anhcraft.oreprocessor.integration.shop;

import me.gypopo.economyshopgui.api.EconomyShopGUIHook;
import me.gypopo.economyshopgui.objects.ShopItem;
import me.gypopo.economyshopgui.util.EconomyType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("ConstantValue")
public class EconomyShopGUIBridge implements ShopProvider {
    @Override
    public boolean canSell(Material material) {
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(new ItemStack(material));
        return shopItem != null && !shopItem.isHidden() && shopItem.getEcoType().getType() == EconomyType.VAULT;
    }

    @Override
    public double getSellPrice(Material material, int amount) {
        ItemStack itemStack = new ItemStack(material, amount);
        ShopItem shopItem = EconomyShopGUIHook.getShopItem(itemStack);
        Double sellPrice = EconomyShopGUIHook.getItemSellPrice(shopItem, itemStack);
        return sellPrice == null ? 0 : sellPrice;
    }
}
