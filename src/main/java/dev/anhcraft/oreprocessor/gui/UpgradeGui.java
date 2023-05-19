package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.palette.ui.Gui;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeGui extends Gui {
    @Description("The lore indicating that throughput upgrade is available")
    @Validation(notNull = true)
    public List<String> throughputUpgradableLore;

    @Description("The lore indicating that throughput upgrade is unavailable due to insufficient balance")
    @Validation(notNull = true)
    public List<String> throughputUnaffordableLore;

    @Description("The lore indicating that throughput upgrade has been maximized")
    @Validation(notNull = true)
    public List<String> throughputMaximizedLore;

    @Description("The lore indicating that capacity upgrade is available")
    @Validation(notNull = true)
    public List<String> capacityUpgradableLore;

    @Description("The lore indicating that capacity upgrade is unavailable due to insufficient balance")
    @Validation(notNull = true)
    public List<String> capacityUnaffordableLore;

    @Description("The lore indicating that capacity upgrade has been maximized")
    @Validation(notNull = true)
    public List<String> capacityMaximizedLore;
}
