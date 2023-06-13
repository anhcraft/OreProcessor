package dev.anhcraft.oreprocessor.gui;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Description;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.config.bukkit.utils.ItemBuilder;
import dev.anhcraft.palette.ui.Gui;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class UpgradeGui extends Gui {
    @Description("The lore indicating that throughput upgrade is available")
    @Validation(notNull = true)
    private ItemBuilder throughputUpgradableIcon;

    @Description("The lore indicating that throughput upgrade is unavailable due to insufficient balance")
    @Validation(notNull = true)
    private ItemBuilder throughputUnaffordableIcon;

    @Description("The lore indicating that throughput upgrade has been maximized")
    @Validation(notNull = true)
    private ItemBuilder throughputMaximizedIcon;

    @Description("The lore indicating that capacity upgrade is available")
    @Validation(notNull = true)
    private ItemBuilder capacityUpgradableIcon;

    @Description("The lore indicating that capacity upgrade is unavailable due to insufficient balance")
    @Validation(notNull = true)
    private ItemBuilder capacityUnaffordableIcon;

    @Description("The lore indicating that capacity upgrade has been maximized")
    @Validation(notNull = true)
    private ItemBuilder capacityMaximizedIcon;

    public ItemBuilder getThroughputUpgradableIcon() {
        return throughputUpgradableIcon.duplicate();
    }

    public ItemBuilder getThroughputUnaffordableIcon() {
        return throughputUnaffordableIcon.duplicate();
    }

    public ItemBuilder getThroughputMaximizedIcon() {
        return throughputMaximizedIcon.duplicate();
    }

    public ItemBuilder getCapacityUpgradableIcon() {
        return capacityUpgradableIcon.duplicate();
    }

    public ItemBuilder getCapacityUnaffordableIcon() {
        return capacityUnaffordableIcon.duplicate();
    }

    public ItemBuilder getCapacityMaximizedIcon() {
        return capacityMaximizedIcon.duplicate();
    }
}
