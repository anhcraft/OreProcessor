package dev.anhcraft.oreprocessor.config;

import dev.anhcraft.config.annotations.Configurable;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class PurgeStatsSettings {
    public int maxPlayerRecords = 1000;
    public int maxServerRecords = 20000;
}
