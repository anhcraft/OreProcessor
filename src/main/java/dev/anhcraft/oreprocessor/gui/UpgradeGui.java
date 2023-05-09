package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.palette.ui.Gui;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeGui extends Gui {
    public List<String> throughputUpgradableLore;
    public List<String> throughputUnaffordableLore;
    public List<String> throughputMaximizedLore;
    public List<String> capacityUpgradableLore;
    public List<String> capacityUnaffordableLore;
    public List<String> capacityMaximizedLore;
}
