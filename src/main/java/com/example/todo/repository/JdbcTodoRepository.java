package com.example.todo.repository;

import com.example.todo.database.ConnectionProvider;
import com.example.todo.model.Todo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcTodoRepository implements TodoRepository {

    private static final String SELECT_COLUMNS = """
            SELECT
                id,
                list_id,
                title,
                description,
                status,
                created_at,
                updated_at
            FROM todos
            """;

    private final ConnectionProvider connectionProvider;

    public JdbcTodoRepository(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public List<Todo> findAll() {

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
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find todos", e);
        }

        return todos;
    }

    @Override
    public Optional<Todo> findById(long id) {

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
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find todo: " + id, e);
        }
    }

    @Override
    public void create(Todo todo) {

        String sql = """
                INSERT INTO todos (
                    list_id,
                    title,
                    description,
                    status
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection =
                        connectionProvider.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(1, todo.getListId());
            statement.setString(2, todo.getTitle());
            statement.setString(3, todo.getDescription());
            statement.setString(4, todo.getStatus().name());

            int updatedRows = statement.executeUpdate();

            if (updatedRows != 1) {
                throw new SQLException(
                        "Unexpected inserted row count: "
                                + updatedRows
                );
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to create todo", e);
        }
    }

    @Override
    public boolean update(Todo todo) {

        String sql = """
                UPDATE todos
                   SET title = ?,
                       description = ?,
                       status = ?,
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
            statement.setString(3, todo.getStatus().name());
            statement.setLong(4, todo.getId());

            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RepositoryException(
                    "Failed to update todo: " + todo.getId(), e);
        }
    }

    @Override
    public boolean delete(long id) {

        String sql = """
                DELETE FROM todos
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
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete todo: " + id, e);
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
                resultSet.getLong("list_id"),
                resultSet.getString("title"),
                resultSet.getString("description"),
                Todo.Status.valueOf(resultSet.getString("status")),
                createdAt,
                updatedAt
        );
    }
}
