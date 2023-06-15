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

    @Description("Quick-sell available icon")
    @Validation(notNull = true)
    private ItemBuilder quickSellAvailableIcon;

    @Description("Quick-sell empty icon")
    @Validation(notNull = true)
    private ItemBuilder quickSellEmptyIcon;

    public ItemBuilder getProductIcon() {
        return productIcon.duplicate();
    }

    public ItemBuilder getQuickSellAvailableIcon() {
        return quickSellAvailableIcon.duplicate();
    }

    public ItemBuilder getQuickSellEmptyIcon() {
        return quickSellEmptyIcon.duplicate();
    }
}
