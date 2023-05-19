package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.palette.ui.Gui;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MenuGui extends Gui {
    @Description("Name for the ore's icon")
    @Validation(notNull = true)
    public String oreName;

    @Description("Lore for the ore's icon")
    @Validation(notNull = true)
    public List<String> oreLore;
}
