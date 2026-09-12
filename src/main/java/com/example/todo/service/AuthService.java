package com.example.todo.service;

import com.example.todo.model.AuthenticatedUser;

import java.util.Optional;

public interface AuthService {
    Optional<AuthenticatedUser> authenticate(
            String name,
            char[] rawPassword
    );
}