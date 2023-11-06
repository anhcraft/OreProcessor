package dev.anhcraft.oreprocessor.storage;

import dev.anhcraft.config.annotations.Configurable;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class SQLCommand {
    public String createPlayerTable;
}
