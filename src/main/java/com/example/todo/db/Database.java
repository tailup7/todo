package com.example.todo.db;

import jakarta.servlet.ServletContext;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import java.sql.DriverManager;
import java.util.Optional;

public final class Database {

    private Database() {
    }

    public static ConnectionProvider create(ServletContext servletContext) {

        String defaultMode = Optional
                .ofNullable(servletContext.getInitParameter("db.mode"))
                .orElse("jndi");

        String mode = Optional
                .ofNullable(System.getenv("TODO_DB_MODE"))
                .orElse(defaultMode);

        if ("direct".equalsIgnoreCase(mode)) {
            return createDirectProvider();
        }

        if (!"jndi".equalsIgnoreCase(mode)) {
            throw new IllegalStateException(
                    "Unsupported TODO_DB_MODE: " + mode
            );
        }

        return createJndiProvider();
    }

    private static ConnectionProvider createJndiProvider() {

        try {
            InitialContext initialContext = new InitialContext();

            DataSource dataSource = (DataSource) initialContext.lookup(
                    "java:/comp/env/jdbc/TodoDB"
            );

            return dataSource::getConnection;

        } catch (NamingException e) {
            throw new IllegalStateException(
                    "JNDI DataSource jdbc/TodoDB was not found",
                    e
            );
        }
    }

    private static ConnectionProvider createDirectProvider() {

        String url = requireEnvironment("TODO_DB_URL");
        String user = requireEnvironment("TODO_DB_USER");
        String password = requireEnvironment("TODO_DB_PASSWORD");

        return () -> DriverManager.getConnection(
                url,
                user,
                password
        );
    }

    private static String requireEnvironment(String name) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Required environment variable is missing: " + name
            );
        }

        return value;
    }
}

