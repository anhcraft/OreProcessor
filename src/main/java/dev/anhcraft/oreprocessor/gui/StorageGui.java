package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.palette.ui.Gui;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class StorageGui extends Gui {
    @Description("The lore indicating that quick-sell feature is available")
    @Validation(notNull = true)
    public List<String> quickSellAvailableLore;

    @Description("The lore indicating that quick-sell feature is unavailable")
    @Validation(notNull = true)
    public List<String> quickSellUnavailableLore;
}
