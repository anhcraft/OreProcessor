package dev.anhcraft.oreprocessor.storage.player.db;

import com.zaxxer.hikari.HikariDataSource;
import dev.anhcraft.oreprocessor.OreProcessor;
import dev.anhcraft.oreprocessor.api.data.PlayerData;
import dev.anhcraft.oreprocessor.storage.SQLCommand;
import dev.anhcraft.oreprocessor.storage.player.IPlayerDataStorage;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.UUID;
import java.util.function.Consumer;

public class DatabaseManager implements IPlayerDataStorage {
    private final OreProcessor plugin;
    private final String table;
    private final HikariDataSource dataSource;

    public DatabaseManager(OreProcessor plugin, String table, HikariDataSource dataSource) {
        this.plugin = plugin;
        this.table = table;
        this.dataSource = dataSource;
        try {
            initializeTable();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public SQLCommand getSQLCommand() {
        return plugin.sqlCommand;
    }

    private int executeUpdate(String cmd, @Nullable Consumer<PreparedStatement> consumer) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            cmd = String.format(cmd, table);
            try (PreparedStatement statement = connection.prepareStatement(cmd)) {
                if (consumer != null) {
                    consumer.accept(statement);
                }
                return statement.executeUpdate();
            }
        }
    }

    private void initializeTable() throws SQLException {
        executeUpdate(getSQLCommand().createPlayerTable, null);
        plugin.getLogger().info("Table " + table + " initialized");
    }

    @Override
    public PlayerData loadOrCreate(UUID id) {
        return new DBPlayerData(id.toString(), this);
    }
}
