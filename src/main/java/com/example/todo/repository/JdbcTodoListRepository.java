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

public final class JdbcTodoListRepository
        implements TodoListRepository {

    private final ConnectionProvider connectionProvider;

    public JdbcTodoListRepository(
            ConnectionProvider connectionProvider) {

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