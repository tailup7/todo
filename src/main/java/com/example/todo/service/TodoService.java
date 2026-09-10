package com.example.todo.service;

import com.example.todo.model.Todo;

import java.util.List;

// Serviceは、画面やHTTPの詳細から独立してToDoの業務処理を提供する。
public interface TodoService {

    List<Todo> findAll();

    Todo findById(long id);

    void create(Todo todo);

    void update(long id, Todo todo);

    void delete(long id);
}
