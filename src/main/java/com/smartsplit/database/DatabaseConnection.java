package com.smartsplit.database;

import com.smartsplit.exception.DatabaseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens local SQLite connections and enables foreign-key enforcement per connection. */
public final class DatabaseConnection {
    private static final Path DATABASE_DIRECTORY = Path.of("database");
    private static final String DATABASE_URL = "jdbc:sqlite:database/smartsplit.db";

    private DatabaseConnection() { }

    public static Connection getConnection() {
        try {
            Files.createDirectories(DATABASE_DIRECTORY);
            Connection connection = DriverManager.getConnection(DATABASE_URL);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (Exception ex) {
            throw new DatabaseException("Unable to connect to the local SQLite database.", ex);
        }
    }
}
