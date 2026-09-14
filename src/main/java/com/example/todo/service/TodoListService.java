package com.example.todo.service;

import java.util.List;

import com.example.todo.model.TodoList;

public interface TodoListService {
    List<TodoList> findByUserId(long userId);

    TodoList findByIdAndUserId(long listId,long userId);

    void create(long userId, String listName);

    void rename(long listId, long userId, String listName);

    void delete(long listId, long userId);
}
