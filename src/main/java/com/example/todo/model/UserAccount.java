package com.example.todo.model;


public record UserAccount(
        long id,
        String name,
        String passwordHash
) {
}
