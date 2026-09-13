package com.example.todo.model;

import java.time.Instant;

public record TodoList(
        long id,
        long userId,
        String listName,
        Instant createdAt) {
}