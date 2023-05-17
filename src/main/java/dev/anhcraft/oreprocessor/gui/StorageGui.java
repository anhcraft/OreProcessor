package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.palette.ui.Gui;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class StorageGui extends Gui {
    public List<String> quickSellAvailableLore;
    public List<String> quickSellUnavailableLore;
}
