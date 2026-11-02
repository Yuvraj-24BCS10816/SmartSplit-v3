package com.smartsplit.dao;

import com.smartsplit.database.DatabaseConnection;
import com.smartsplit.exception.DatabaseException;
import com.smartsplit.model.User;
import com.smartsplit.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Account persistence, authentication verification and legacy hash migration. */
public class UserDAO {

    public Optional<User> authenticate(String username, String password) {
        String sql = "SELECT user_id, name, username, email, password_hash FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            String storedHash;
            User user;
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return Optional.empty();
                storedHash = results.getString("password_hash");
                if (!PasswordUtil.verify(password, storedHash)) return Optional.empty();
                user = map(results);
            }

            // Upgrade v2 unsalted SHA-256 hashes after successful verification without invalidating the account.
            if (PasswordUtil.isLegacyHash(storedHash)) {
                String updateSql = "UPDATE users SET password_hash = ? WHERE user_id = ? AND password_hash = ?";
                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setString(1, PasswordUtil.hash(password));
                    update.setInt(2, user.getId());
                    update.setString(3, storedHash);
                    update.executeUpdate();
                }
            }
            return Optional.of(user);
        } catch (SQLException ex) {
            throw new DatabaseException("Login query failed.", ex);
        }
    }

    public int create(User user, String passwordHash) {
        String sql = """
                INSERT INTO users(name, username, password_hash, email, created_at)
                VALUES(?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getUsername());
            statement.setString(3, passwordHash);
            statement.setString(4, user.getEmail());
            statement.setString(5, LocalDateTime.now().toString());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not create user. Username or email may already exist.", ex);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT user_id, name, username, email FROM users WHERE username = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("User lookup failed.", ex);
        }
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, name, username, email FROM users ORDER BY name";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) users.add(map(results));
            return users;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load users.", ex);
        }
    }

    public List<User> searchByUsername(String term) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, name, username, email FROM users WHERE username LIKE ? ORDER BY name";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + term + "%");
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) users.add(map(results));
            }
            return users;
        } catch (SQLException ex) {
            throw new DatabaseException("User search failed.", ex);
        }
    }

    private User map(ResultSet results) throws SQLException {
        return new User(
                results.getInt("user_id"),
                results.getString("name"),
                results.getString("username"),
                results.getString("email")
        );
    }
}
