package com.example.todo.model;

import java.time.Instant;

public class Todo {

    public enum Status {
        NOT_STARTED("未開始"),
        IN_PROGRESS("実行中"),
        COMPLETED("完了");
        private final String label;
        Status(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }

    private long id;
    private long listId;
    private String title;
    private String description;
    private Status status = Status.NOT_STARTED;
    private Instant createdAt;
    private Instant updatedAt;

    public Todo() {
    }

    public Todo(
        long id,
        long listId,
        String title,
        String description,
        Status status,
        Instant createdAt,
        Instant updatedAt) {

        this.id = id;
        this.listId = listId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}