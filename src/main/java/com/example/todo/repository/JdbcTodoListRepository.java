package com.example.todo.repository;

import com.example.todo.database.ConnectionProvider;
import com.example.todo.model.TodoList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcTodoListRepository
        implements TodoListRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcTodoListRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<TodoList> findByUserId(long userId) {

        String sql = """
                SELECT
                    id,
                    user_id,
                    list_name,
                    created_at
                FROM todos_list
                WHERE user_id = ?
                ORDER BY created_at ASC, id ASC
                """;

        List<TodoList> todoLists = new ArrayList<>();

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(1, userId);

            try (ResultSet resultSet =
                    statement.executeQuery()) {

                while (resultSet.next()) {
                    todoLists.add(map(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to find todo lists for user: " + userId,
                    e
            );
        }

        return todoLists;
    }

    @Override
    public Optional<TodoList> findByIdAndUserId(long listId, long userId) {

        String sql = """
                SELECT
                    id,
                    user_id,
                    list_name,
                    created_at
                FROM todos_list
                WHERE id = ?
                  AND user_id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, listId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(map(resultSet));
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to find todo list: " + listId,
                    e
            );
        }
    }

    @Override
    public boolean existsByIdAndUserId(long listId, long userId) {

        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM todos_list
                    WHERE id = ?
                    AND user_id = ?
                )
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, listId);
            statement.setLong(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to verify todo list ownership",
                    e
            );
        }
    }
    
    @Override
    public void create(long userId, String listName) {

        String sql = """
                INSERT INTO todos_list (
                    user_id,
                    list_name
                )
                VALUES (?, ?)
                """;

        try (
                Connection connection = connectionProvider.getConnection();

                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, userId);
            statement.setString(2, listName);

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new SQLException(
                        "Unexpected inserted row count: "
                                + updatedRows
                );
            }
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to create todo list",
                    e
            );
        }
    }

    @Override
    public boolean updateName(long listId, long userId, String listName) {

        String sql = """
                UPDATE todos_list
                SET list_name = ?
                WHERE id = ?
                  AND user_id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, listName);
            statement.setLong(2, listId);
            statement.setLong(3, userId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to update todo list: " + listId,
                    e
            );
        }
    }

    @Override
    public boolean deleteByIdAndUserId(long listId, long userId) {

        String sql = """
                DELETE FROM todos_list
                WHERE id = ?
                  AND user_id = ?
                """;

        try (
                Connection connection = connectionProvider.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, listId);
            statement.setLong(2, userId);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to delete todo list: " + listId,
                    e
            );
        }
    }

    private TodoList map(ResultSet resultSet)
            throws SQLException {

        Instant createdAt =
                resultSet.getTimestamp("created_at")
                        .toInstant();

        return new TodoList(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("list_name"),
                createdAt
        );
    }
}