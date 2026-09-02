package com.example.todo.dao;

import com.example.todo.db.ConnectionProvider;
import com.example.todo.model.Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcTodoDao implements TodoDao {

    private static final String SELECT_COLUMNS = """
            SELECT
                id,
                title,
                description,
                completed,
                created_at,
                updated_at
            FROM todo
            """;

    private final ConnectionProvider connectionProvider;

    public JdbcTodoDao(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Todo> findAll() throws SQLException {

        String sql = SELECT_COLUMNS + """
                 ORDER BY id DESC
                """;

        List<Todo> todos = new ArrayList<>();

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {
                todos.add(map(resultSet));
            }
        }

        return todos;
    }

    @Override
    public Optional<Todo> findById(long id)
            throws SQLException {

        String sql = SELECT_COLUMNS + """
                 WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(map(resultSet));
            }
        }
    }

    @Override
    public void create(Todo todo)
            throws SQLException {

        String sql = """
                INSERT INTO todo (
                    title,
                    description,
                    completed
                )
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, todo.getTitle());
            statement.setString(2, todo.getDescription());
            statement.setBoolean(3, todo.isCompleted());

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new SQLException(
                        "Unexpected inserted row count: "
                                + updatedRows
                );
            }
        }
    }

    @Override
    public boolean update(Todo todo)
            throws SQLException {

        String sql = """
                UPDATE todo
                   SET title = ?,
                       description = ?,
                       completed = ?,
                       updated_at = CURRENT_TIMESTAMP
                 WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, todo.getTitle());
            statement.setString(2, todo.getDescription());
            statement.setBoolean(3, todo.isCompleted());
            statement.setLong(4, todo.getId());

            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(long id)
            throws SQLException {

        String sql = """
                DELETE FROM todo
                 WHERE id = ?
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(1, id);

            return statement.executeUpdate() == 1;
        }
    }

    private Todo map(ResultSet resultSet)
            throws SQLException {

        Instant createdAt =
                resultSet
                        .getTimestamp("created_at")
                        .toInstant();

        Instant updatedAt =
                resultSet
                        .getTimestamp("updated_at")
                        .toInstant();

        return new Todo(
                resultSet.getLong("id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getBoolean("completed"),
                createdAt,
                updatedAt
        );
    }
}
