package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Validation;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MessageConfig {
    @Validation(notNull = true)
    public String prefix;
    @Validation(notNull = true)
    public String upgradeThroughputSuccess;
    @Validation(notNull = true)
    public String upgradeThroughputFailed;
    @Validation(notNull = true)
    public String upgradeCapacitySuccess;
    @Validation(notNull = true)
    public String upgradeCapacityFailed;
    @Validation(notNull = true)
    public String storageFull;
    @Validation(notNull = true)
    public String[] firstTimeTutorial;
    @Validation(notNull = true)
    public String quickSellSuccess;
    @Validation(notNull = true)
    public String[] statisticCumulativeDetails;
    public String emptyHand = "&cYour hand is empty";
    public String storeInvalidItem = "&cThis item is inappropriate";
    public String cannotStoreItem = "&cFailed due to inappropriate item or no space left";
    public String storedItems = "&aStored &f{amount}&a items into &f{ores}&a storage!";
}
