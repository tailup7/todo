package com.example.todo.service;

public final class TodoListNotFoundException
        extends RuntimeException {

    private final long listId;

    public TodoListNotFoundException(long listId) {
        super("Todo list not found: " + listId);
        this.listId = listId;
    }

    public long getListId() {
        return listId;
    }
}