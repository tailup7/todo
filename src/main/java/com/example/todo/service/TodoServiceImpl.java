package com.example.todo.service;

import com.example.todo.model.Todo;
import com.example.todo.repository.TodoRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TodoServiceImpl
        implements TodoService {

    private final TodoRepository todoRepository;

    public TodoServiceImpl(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Override
    public List<Todo> findAll() {
        return todoRepository.findAll();
    }

    @Override
    public Todo findById(long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Override
    public void create(Todo todo) {
        validate(todo);
        todoRepository.create(todo);
    }

    @Override
    public void update(long id, Todo todo) {
        Todo existing = findById(id);

        // 更新対象のIDと作成・更新日時は、画面入力ではなく既存データを基準にする。
        todo.setId(id);
        todo.setCreatedAt(existing.getCreatedAt());
        todo.setUpdatedAt(existing.getUpdatedAt());

        validate(todo);

        if (!todoRepository.update(todo)) {
            throw new TodoNotFoundException(id);
        }
    }

    @Override
    public void delete(long id) {
        if (!todoRepository.delete(id)) {
            throw new TodoNotFoundException(id);
        }
    }

    private void validate(Todo todo) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (todo.getTitle() == null || todo.getTitle().isBlank()) {
            errors.put("title", "タイトルは必須です。");
        } else if (todo.getTitle().length() > 200) {
            errors.put("title", "タイトルは200文字以内です。");
        }

        if (todo.getDescription() != null
                && todo.getDescription().length() > 2000) {
            errors.put("description", "説明は2000文字以内です。");
        }

        if (todo.getStatus() == null) {
        errors.put("status", "状態を選択してください。");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
