package com.smartsplit.dao;

import com.smartsplit.database.DatabaseConnection;
import com.smartsplit.exception.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Read-only persistence access for expense categories. */
public class CategoryDAO {

    public Map<Integer, String> findAll() {
        Map<Integer, String> categories = new LinkedHashMap<>();
        String sql = "SELECT category_id, name FROM categories ORDER BY name";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                categories.put(results.getInt("category_id"), results.getString("name"));
            }
            return categories;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load categories.", ex);
        }
    }
}
