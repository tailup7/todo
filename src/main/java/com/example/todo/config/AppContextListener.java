package com.example.todo.config;

import com.example.todo.database.Database;
import com.example.todo.repository.JdbcTodoRepository;
import com.example.todo.repository.TodoRepository;
import com.example.todo.service.TodoService;
import com.example.todo.service.TodoServiceImpl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public final class AppContextListener
        implements ServletContextListener {

    @Override
    public void contextInitialized(
            ServletContextEvent event) {

        ServletContext context =
                event.getServletContext();

        try {
            TodoRepository todoRepository =
                    new JdbcTodoRepository(
                            Database.create(context));

            TodoService todoService =
                    new TodoServiceImpl(todoRepository);

            context.setAttribute(
                    "todoService",
                    todoService
            );

            context.log(
                    "Todo application initialized"
            );

        } catch (RuntimeException e) {

            context.log(
                    "Todo application initialization failed",
                    e
            );

            throw e;
        }
    }
}
