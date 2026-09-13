package com.example.todo.repository;

import com.example.todo.model.TodoList;

import java.util.List;

public interface TodoListRepository {

    List<TodoList> findByUserId(long userId);
}