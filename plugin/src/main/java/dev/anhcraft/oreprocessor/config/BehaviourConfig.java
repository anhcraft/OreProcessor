package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class BehaviourConfig {
    public boolean dropOnFullStorage;

    public boolean enableMiningStatOnFullStorage;

    public boolean disableOfflineProcessing;
}
