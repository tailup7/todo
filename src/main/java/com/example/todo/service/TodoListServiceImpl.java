package com.example.todo.service;

import com.example.todo.model.TodoList;
import com.example.todo.repository.TodoListRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TodoListServiceImpl
        implements TodoListService {

    private final TodoListRepository todoListRepository;

    public TodoListServiceImpl(TodoListRepository todoListRepository) {
        this.todoListRepository = todoListRepository;
    }

    @Override
    public List<TodoList> findByUserId(long userId) {
        return todoListRepository.findByUserId(userId);
    }

    @Override
    public TodoList findByIdAndUserId(long listId, long userId) {
        return todoListRepository.findByIdAndUserId(listId, userId).orElseThrow( () -> new TodoListNotFoundException(listId));
    }

    @Override
    public void create(long userId, String listName) {
        String normalizedListName = validateListName(listName);
        todoListRepository.create(userId, normalizedListName);
    }

    @Override
    public void rename(long listId, long userId, String listName) {

        String normalizedListName = validateListName(listName);

        boolean updated = todoListRepository.updateName(listId, userId, normalizedListName);

        if (!updated) {
            throw new TodoListNotFoundException(listId);
        }
    }

    @Override
    public void delete(long listId, long userId) {
        boolean deleted =todoListRepository.deleteByIdAndUserId(listId, userId);
        if (!deleted) {
            throw new TodoListNotFoundException(listId);
        }
    }

    private String validateListName(String listName) {

        String normalized =
                listName == null
                        ? ""
                        : listName.trim();

        Map<String, String> errors = new LinkedHashMap<>();

        if (normalized.isEmpty()) {
            errors.put(
                    "listName",
                    "リスト名は必須です。"
            );
        } else if (normalized.length() > 100) {
            errors.put(
                    "listName",
                    "リスト名は100文字以内です。"
            );
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        return normalized;
    }
}