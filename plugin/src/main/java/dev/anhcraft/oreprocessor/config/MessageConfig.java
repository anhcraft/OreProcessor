package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Validation;

import java.util.List;

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
    public List<String> firstTimeTutorial;
    @Validation(notNull = true)
    public String quickSellSuccess;
}
