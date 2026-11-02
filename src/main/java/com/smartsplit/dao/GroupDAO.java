package com.smartsplit.dao;

import com.smartsplit.database.DatabaseConnection;
import com.smartsplit.exception.DatabaseException;
import com.smartsplit.model.Group;
import com.smartsplit.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC persistence for groups and their participant memberships. */
public class GroupDAO {

    public int create(Group group) {
        String sql = "INSERT INTO expense_groups(name, description, created_by, created_at) VALUES(?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, group.getName());
            statement.setString(2, group.getDescription());
            statement.setInt(3, group.getCreatedBy());
            statement.setString(4, LocalDateTime.now().toString());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not create group.", ex);
        }
    }

    public List<Group> findByUser(int userId) {
        List<Group> groups = new ArrayList<>();
        String sql = """
                SELECT g.*
                FROM expense_groups g
                JOIN group_members gm ON gm.group_id = g.group_id
                WHERE gm.user_id = ?
                ORDER BY g.created_at DESC
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) groups.add(map(results));
            }
            return groups;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load groups.", ex);
        }
    }

    public Optional<Group> findById(int groupId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM expense_groups WHERE group_id = ?")) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load group.", ex);
        }
    }

    public void addMember(int groupId, int userId) {
        String sql = "INSERT OR IGNORE INTO group_members(group_id, user_id, joined_at) VALUES(?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.setString(3, LocalDateTime.now().toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("Could not add member to group.", ex);
        }
    }

    /** Returns true if a participant has any stored financial history in this group. */
    public boolean hasFinancialActivity(int groupId, int userId) {
        String sql = """
                SELECT
                    EXISTS(SELECT 1 FROM expenses WHERE group_id = ? AND paid_by = ?)
                    OR EXISTS(
                        SELECT 1
                        FROM expense_splits es
                        JOIN expenses e ON e.expense_id = es.expense_id
                        WHERE e.group_id = ? AND es.user_id = ?
                    )
                    OR EXISTS(
                        SELECT 1 FROM settlements
                        WHERE group_id = ? AND (payer_id = ? OR receiver_id = ?)
                    )
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.setInt(3, groupId);
            statement.setInt(4, userId);
            statement.setInt(5, groupId);
            statement.setInt(6, userId);
            statement.setInt(7, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() && results.getInt(1) != 0;
            }
        } catch (SQLException ex) {
            throw new DatabaseException("Could not check member financial activity.", ex);
        }
    }

    public void removeMember(int groupId, int userId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM group_members WHERE group_id = ? AND user_id = ?")) {
            statement.setInt(1, groupId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("Could not remove member from group.", ex);
        }
    }

    public List<Member> members(int groupId) {
        List<Member> members = new ArrayList<>();
        String sql = """
                SELECT u.user_id, u.name, u.username, u.email
                FROM users u
                JOIN group_members gm ON gm.user_id = u.user_id
                WHERE gm.group_id = ?
                ORDER BY u.name
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, groupId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    members.add(new Member(
                            results.getInt("user_id"),
                            results.getString("name"),
                            results.getString("username"),
                            results.getString("email")
                    ));
                }
            }
            return members;
        } catch (SQLException ex) {
            throw new DatabaseException("Could not load group members.", ex);
        }
    }

    public void delete(int groupId) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM expense_groups WHERE group_id = ?")) {
            statement.setInt(1, groupId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new DatabaseException("Could not delete group.", ex);
        }
    }

    private Group map(ResultSet results) throws SQLException {
        return new Group(
                results.getInt("group_id"),
                results.getString("name"),
                results.getString("description"),
                results.getInt("created_by"),
                LocalDateTime.parse(results.getString("created_at"))
        );
    }
}
