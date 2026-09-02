package com.example.todo.dao;

import com.example.todo.model.Todo;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface TodoDao {

    List<Todo> findAll() throws SQLException;

    Optional<Todo> findById(long id) throws SQLException;

    void create(Todo todo) throws SQLException;

    boolean update(Todo todo) throws SQLException;

    boolean delete(long id) throws SQLException;
}
