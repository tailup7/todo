package com.example.todo.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class BCryptPasswordHasher implements PasswordHasher {

    @Override
    public boolean matches(char[] rawPassword, String passwordHash) {
        return BCrypt.verifyer().verify(rawPassword, passwordHash).verified;
    }
}