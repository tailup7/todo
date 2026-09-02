package com.example.todo.web;

import com.example.todo.dao.JdbcTodoDao;
import com.example.todo.db.Database;

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
            JdbcTodoDao todoDao =
                    new JdbcTodoDao(
                            Database.create(context)
                    );

            context.setAttribute(
                    "todoDao",
                    todoDao
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
