// DAO : Data Access Object。Todoデータをデータベースなどから取得・保存するための操作方法を定義したインターフェース
// TodoRepository インターフェースにて、TodoデータのCRUD操作を定義し、
// その裏側でJdbcTodoRepositoryがSQL文によって実際のデータベース操作を行う。
package com.example.todo.repository;

import com.example.todo.model.Todo;

import java.util.List;
import java.util.Optional;

public interface TodoRepository {

    List<Todo> findAll();

    Optional<Todo> findById(long id);

    void create(Todo todo);

    boolean update(Todo todo);

    boolean delete(long id);
}
