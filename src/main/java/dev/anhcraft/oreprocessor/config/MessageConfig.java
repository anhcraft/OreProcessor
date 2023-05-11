package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;

import java.util.List;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class MessageConfig {
    public String prefix;
    public String upgradeSuccess;
    public String upgradeFailed;
    public String storageFull;
    public List<String> firstTimeTutorial;
}
