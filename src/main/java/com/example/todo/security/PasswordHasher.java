package com.example.todo.security;

public interface PasswordHasher {
    boolean matches(char[] rawPassword, String passwordHash);
}
