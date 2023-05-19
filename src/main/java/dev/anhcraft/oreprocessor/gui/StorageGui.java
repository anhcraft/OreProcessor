package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.palette.ui.Gui;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class StorageGui extends Gui {
    @Description("The icon indicating that quick-sell feature is available")
    @Validation(notNull = true)
    public ItemBuilder quickSellAvailable;

    @Description("The icon indicating that quick-sell feature is unavailable")
    @Validation(notNull = true)
    public ItemBuilder quickSellUnavailable;
}
