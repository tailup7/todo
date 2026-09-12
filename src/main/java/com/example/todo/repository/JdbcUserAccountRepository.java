package com.example.todo.repository;

import com.example.todo.database.ConnectionProvider;
import com.example.todo.model.UserAccount;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Optional;

public final class JdbcUserAccountRepository implements UserAccountRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcUserAccountRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public Optional<UserAccount> findByName(String name) {

        String sql = """
                SELECT
                    id,
                    name,
                    password_hash
                FROM user_account
                WHERE name = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(
                        new UserAccount(
                                resultSet.getLong("id"),
                                resultSet.getString("name"),
                                resultSet.getString("password_hash")
                        )
                );
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find user account: " + name, e);
        }
    }
}