package com.example.todo.service;

public final class TodoNotFoundException
        extends RuntimeException {

    public TodoNotFoundException(long id) {
        super("Todo not found: " + id);
    }
}
