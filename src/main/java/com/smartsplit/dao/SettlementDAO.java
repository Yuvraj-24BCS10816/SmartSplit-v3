package com.smartsplit.dao;

import com.smartsplit.database.DatabaseConnection;
import com.smartsplit.exception.DatabaseException;
import com.smartsplit.model.Settlement;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Persists settlement suggestions and completed payments. */
public class SettlementDAO {

    public int save(Settlement settlement) {
        String sql = """
                INSERT INTO settlements(group_id, payer_id, receiver_id, amount, settlement_date, status)
                VALUES(?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, settlement.getGroupId());
            statement.setInt(2, settlement.getPayerId());
            statement.setInt(3, settlement.getReceiverId());
            statement.setBigDecimal(4, settlement.getAmount());
            statement.setString(5, settlement.getDate().toString());
            statement.setString(6, settlement.getStatus());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not save settlement.", ex);
        }
    }

    public boolean hasPending(int groupId, int payerId, int receiverId, BigDecimal amount) {
        String sql = """
                SELECT 1 FROM settlements
                WHERE group_id = ? AND payer_id = ? AND receiver_id = ?
                  AND amount = ? AND status = 'PENDING'
                LIMIT 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            statement.setInt(2, payerId);
            statement.setInt(3, receiverId);
            statement.setBigDecimal(4, amount);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not check for duplicate pending settlements.", ex);
        }
    }

    public void complete(int settlementId) {
        String sql = "UPDATE settlements SET status = 'COMPLETED' WHERE settlement_id = ? AND status = 'PENDING'";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, settlementId);
            if (statement.executeUpdate() == 0) {
                throw new DatabaseException("Settlement is already completed or no longer exists.");
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not update settlement status.", ex);
        }
    }

    public List<Settlement> findByGroup(int groupId) {
        List<Settlement> settlements = new ArrayList<>();
        String sql = """
                SELECT s.*, payer.name AS payer_name, receiver.name AS receiver_name
                FROM settlements s
                JOIN users payer ON payer.user_id = s.payer_id
                JOIN users receiver ON receiver.user_id = s.receiver_id
                WHERE s.group_id = ?
                ORDER BY s.settlement_date DESC, s.settlement_id DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Settlement settlement = new Settlement(
                            results.getInt("settlement_id"),
                            results.getInt("group_id"),
                            results.getInt("payer_id"),
                            results.getInt("receiver_id"),
                            results.getBigDecimal("amount"),
                            LocalDate.parse(results.getString("settlement_date")),
                            results.getString("status")
                    );
                    settlement.setPayerName(results.getString("payer_name"));
                    settlement.setReceiverName(results.getString("receiver_name"));
                    settlements.add(settlement);
                }
            }
            return settlements;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load settlement history.", ex);
        }
    }

    /**
     * Completed payments affect the net position: a payer moves up toward zero,
     * while the receiver moves down toward zero.
     */
    public Map<Integer, BigDecimal> netCompletedSettlements(int groupId) {
        Map<Integer, BigDecimal> effects = new HashMap<>();
        String sql = """
                SELECT payer_id, receiver_id, amount
                FROM settlements
                WHERE group_id = ? AND status = 'COMPLETED'
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    int payerId = results.getInt("payer_id");
                    int receiverId = results.getInt("receiver_id");
                    BigDecimal amount = results.getBigDecimal("amount");
                    effects.merge(payerId, amount, BigDecimal::add);
                    effects.merge(receiverId, amount.negate(), BigDecimal::add);
                }
            }
            return effects;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not calculate settlement effects.", ex);
        }
    }
}
