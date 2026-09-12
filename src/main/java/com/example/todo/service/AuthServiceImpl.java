package com.example.todo.service;

import com.example.todo.model.AuthenticatedUser;
import com.example.todo.model.UserAccount;
import com.example.todo.repository.UserAccountRepository;
import com.example.todo.security.PasswordHasher;

import java.util.Optional;

public final class AuthServiceImpl
        implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;

    public AuthServiceImpl(
            UserAccountRepository userAccountRepository,
            PasswordHasher passwordHasher) {

        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public Optional<AuthenticatedUser> authenticate(
            String name,
            char[] rawPassword) {

        if (name == null
                || name.isBlank()
                || rawPassword == null
                || rawPassword.length == 0) {

            return Optional.empty();
        }

        Optional<UserAccount> account =
                userAccountRepository.findByName(name);

        if (account.isEmpty()) {
            return Optional.empty();
        }

        UserAccount userAccount = account.get();

        if (!passwordHasher.matches(
                rawPassword,
                userAccount.passwordHash())) {

            return Optional.empty();
        }

        return Optional.of(
                new AuthenticatedUser(
                        userAccount.id(),
                        userAccount.name()
                )
        );
    }
}