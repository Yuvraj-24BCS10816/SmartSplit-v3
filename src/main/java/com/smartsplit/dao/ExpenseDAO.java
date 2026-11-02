package com.smartsplit.dao;

import com.smartsplit.database.DatabaseConnection;
import com.smartsplit.exception.DatabaseException;
import com.smartsplit.model.Expense;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** JDBC persistence for expenses and their participant allocations. */
public class ExpenseDAO {

    /** Saves the expense and all of its shares atomically. */
    public int save(Expense expense,
                    Map<Integer, BigDecimal> shares,
                    Map<Integer, BigDecimal> percentages) {
        String insertExpense = """
                INSERT INTO expenses(group_id, description, amount, paid_by, category_id,
                                     split_type, expense_date, created_at)
                VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int expenseId;
                try (PreparedStatement statement = connection.prepareStatement(
                        insertExpense, Statement.RETURN_GENERATED_KEYS)) {
                    statement.setInt(1, expense.getGroupId());
                    statement.setString(2, expense.getDescription());
                    statement.setBigDecimal(3, expense.getAmount());
                    statement.setInt(4, expense.getPaidBy());
                    statement.setInt(5, expense.getCategoryId());
                    statement.setString(6, expense.getSplitType());
                    statement.setString(7, expense.getExpenseDate().toString());
                    statement.setString(8, LocalDateTime.now().toString());
                    statement.executeUpdate();

                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("SQLite did not return an expense identifier.");
                        }
                        expenseId = keys.getInt(1);
                    }
                }

                String insertShare = """
                        INSERT INTO expense_splits(expense_id, user_id, share_amount, percentage)
                        VALUES(?, ?, ?, ?)
                        """;
                try (PreparedStatement statement = connection.prepareStatement(insertShare)) {
                    for (Map.Entry<Integer, BigDecimal> entry : shares.entrySet()) {
                        statement.setInt(1, expenseId);
                        statement.setInt(2, entry.getKey());
                        statement.setBigDecimal(3, entry.getValue());

                        BigDecimal percentage = percentages == null ? null : percentages.get(entry.getKey());
                        if (percentage == null) {
                            statement.setNull(4, Types.NUMERIC);
                        } else {
                            statement.setBigDecimal(4, percentage);
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                connection.commit();
                return expenseId;
            } catch (Exception ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }
                throw ex;
            }
        } catch (Exception ex) {
            throw new DatabaseException("Could not save expense and allocations.", ex);
        }
    }

    public void delete(int expenseId) {
        String sql = "DELETE FROM expenses WHERE expense_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, expenseId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("Could not delete expense.", ex);
        }
    }

    public List<Expense> findByGroup(int groupId) {
        List<Expense> expenses = new ArrayList<>();
        String sql = "SELECT * FROM expenses WHERE group_id = ? ORDER BY expense_date DESC, expense_id DESC";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) expenses.add(map(results));
            }
            return expenses;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load expenses.", ex);
        }
    }

    public Map<Integer, BigDecimal> totalSharesByUser(int groupId) {
        Map<Integer, BigDecimal> totals = new HashMap<>();
        String sql = """
                SELECT es.user_id, SUM(es.share_amount) AS total
                FROM expense_splits es
                JOIN expenses e ON e.expense_id = es.expense_id
                WHERE e.group_id = ?
                GROUP BY es.user_id
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) totals.put(results.getInt("user_id"), results.getBigDecimal("total"));
            }
            return totals;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not calculate member shares.", ex);
        }
    }

    public Map<Integer, BigDecimal> totalPaidByUser(int groupId) {
        Map<Integer, BigDecimal> totals = new HashMap<>();
        String sql = "SELECT paid_by, SUM(amount) AS total FROM expenses WHERE group_id = ? GROUP BY paid_by";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) totals.put(results.getInt("paid_by"), results.getBigDecimal("total"));
            }
            return totals;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not calculate member payments.", ex);
        }
    }

    public BigDecimal totalSpent(int groupId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE group_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? results.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not calculate group spending.", ex);
        }
    }

    private Expense map(ResultSet results) throws SQLException {
        return new Expense(
                results.getInt("expense_id"),
                results.getInt("group_id"),
                results.getString("description"),
                results.getBigDecimal("amount"),
                results.getInt("paid_by"),
                results.getInt("category_id"),
                results.getString("split_type"),
                LocalDate.parse(results.getString("expense_date"))
        );
    }
}
