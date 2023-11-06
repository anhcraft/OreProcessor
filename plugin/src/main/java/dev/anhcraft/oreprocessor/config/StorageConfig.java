package dev.anhcraft.oreprocessor.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.anhcraft.config.annotations.Configurable;
import dev.anhcraft.config.annotations.Optional;
import dev.anhcraft.config.annotations.PostHandler;
import dev.anhcraft.config.annotations.Validation;
import dev.anhcraft.oreprocessor.api.storage.StorageType;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

@Configurable(keyNamingStyle = Configurable.NamingStyle.TRAIN_CASE)
public class StorageConfig {
    @Optional
    public StorageType type = StorageType.FLATFILE;
    @Validation(notNull = true)
    public String playerDataTable;
    @Validation(notNull = true)
    public String serverDataTable;
    public String hikariProperties;

    @PostHandler
    private void check() {
        if (!playerDataTable.matches("[A-Za-z0-9_]+")) {
            throw new RuntimeException("Invalid player data table name");
        }
        if (!serverDataTable.matches("[A-Za-z0-9_]+")) {
            throw new RuntimeException("Invalid server data table name");
        }
    }

    public HikariDataSource createDataSource() {
        Properties props = new Properties();

        if (hikariProperties == null) {
            throw new RuntimeException("No hikari properties provided");
        }

        for (String s : hikariProperties.split("\\\\n")) {
            String[] args = s.split("=");
            props.setProperty(args[0].trim(), args[1].trim());
        }

        return new HikariDataSource(new HikariConfig(props));
    }
}
