package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.palette.ui.Gui;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class StorageGui extends Gui {
    @Description("Product representation")
    @Validation(notNull = true)
    private ItemBuilder productIcon;

    @Description("Quick-sell icon")
    @Validation(notNull = true)
    private ItemBuilder quickSellIcon;

    public ItemBuilder getProductIcon() {
        return productIcon.duplicate();
    }

    public ItemBuilder getQuickSellIcon() {
        return quickSellIcon.duplicate();
    }
}
