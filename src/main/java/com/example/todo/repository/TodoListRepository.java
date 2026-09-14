package com.example.todo.repository;

import com.example.todo.model.TodoList;

import java.util.List;
import java.util.Optional;

public interface TodoListRepository {

    List<TodoList> findByUserId(long userId);

    Optional<TodoList> findByIdAndUserId(long listId, long userId);

    boolean existsByIdAndUserId(long listId, long userId);

    void create(long userId, String listName);

    boolean updateName(long listId, long userId, String listName);

    boolean deleteByIdAndUserId(long listId, long userId);
}