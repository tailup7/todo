package com.example.todo.service;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ValidationException
        extends RuntimeException {

    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Todo validation failed");
        this.errors = Map.copyOf(new LinkedHashMap<>(errors));
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
