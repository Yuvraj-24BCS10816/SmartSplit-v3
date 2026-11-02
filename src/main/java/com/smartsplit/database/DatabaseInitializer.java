package com.smartsplit.database;

import com.smartsplit.util.PasswordUtil;

import java.sql.*;
import java.time.LocalDateTime;

public final class DatabaseInitializer {
    private DatabaseInitializer() {}

    public static void initialize() {
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (user_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,username TEXT NOT NULL UNIQUE,password_hash TEXT NOT NULL,email TEXT NOT NULL UNIQUE,created_at TEXT NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS expense_groups (group_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,description TEXT,created_by INTEGER NOT NULL,created_at TEXT NOT NULL,FOREIGN KEY(created_by) REFERENCES users(user_id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS group_members (group_id INTEGER NOT NULL,user_id INTEGER NOT NULL,joined_at TEXT NOT NULL,PRIMARY KEY(group_id,user_id),FOREIGN KEY(group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS categories (category_id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL UNIQUE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS expenses (expense_id INTEGER PRIMARY KEY AUTOINCREMENT,group_id INTEGER NOT NULL,description TEXT NOT NULL,amount NUMERIC NOT NULL CHECK(amount>0),paid_by INTEGER NOT NULL,category_id INTEGER NOT NULL,split_type TEXT NOT NULL CHECK(split_type IN ('EQUAL','EXACT','PERCENTAGE')),expense_date TEXT NOT NULL,created_at TEXT NOT NULL,FOREIGN KEY(group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,FOREIGN KEY(paid_by) REFERENCES users(user_id),FOREIGN KEY(category_id) REFERENCES categories(category_id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS expense_splits (split_id INTEGER PRIMARY KEY AUTOINCREMENT,expense_id INTEGER NOT NULL,user_id INTEGER NOT NULL,share_amount NUMERIC NOT NULL CHECK(share_amount>=0),percentage NUMERIC,FOREIGN KEY(expense_id) REFERENCES expenses(expense_id) ON DELETE CASCADE,FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE,UNIQUE(expense_id,user_id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS settlements (settlement_id INTEGER PRIMARY KEY AUTOINCREMENT,group_id INTEGER NOT NULL,payer_id INTEGER NOT NULL,receiver_id INTEGER NOT NULL,amount NUMERIC NOT NULL CHECK(amount>0),settlement_date TEXT NOT NULL,status TEXT NOT NULL CHECK(status IN ('PENDING','COMPLETED')),FOREIGN KEY(group_id) REFERENCES expense_groups(group_id) ON DELETE CASCADE,FOREIGN KEY(payer_id) REFERENCES users(user_id),FOREIGN KEY(receiver_id) REFERENCES users(user_id),CHECK(payer_id<>receiver_id))");

            String[] cats = {"Food", "Travel", "Accommodation", "Entertainment", "Shopping", "Bills", "Education", "Other"};
            try (PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO categories(name) VALUES(?)")) {
                for (String cat : cats) {
                    ps.setString(1, cat);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Always keep a ready-to-use demo account.
            seedUser(c, "Demo User", "demo", "demo@smartsplit.local", "demo123");

            // Classroom demo participants. They are ordinary local users and can be added to any group
            // without having to register separate accounts manually.
            seedUser(c, "Rahul Sharma", "rahul", "rahul@smartsplit.local", "demo123");
            seedUser(c, "Aman Verma", "aman", "aman@smartsplit.local", "demo123");
            seedUser(c, "Karan Singh", "karan", "karan@smartsplit.local", "demo123");
        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void seedUser(Connection c, String name, String username, String email, String password) throws SQLException {
        try (PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM users WHERE username=?")) {
            check.setString(1, username);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return;
            }
        }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO users(name,username,password_hash,email,created_at) VALUES(?,?,?,?,?)")) {
            p.setString(1, name);
            p.setString(2, username);
            p.setString(3, PasswordUtil.hash(password));
            p.setString(4, email);
            p.setString(5, LocalDateTime.now().toString());
            p.executeUpdate();
        }
    }
}
