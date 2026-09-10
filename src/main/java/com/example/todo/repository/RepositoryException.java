package com.example.todo.repository;

public final class RepositoryException
        extends RuntimeException {

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
