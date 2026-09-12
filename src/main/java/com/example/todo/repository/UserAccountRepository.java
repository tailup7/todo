package com.example.todo.repository;

import com.example.todo.model.UserAccount;

import java.util.Optional;

public interface UserAccountRepository {
    Optional<UserAccount> findByName(String name);
}