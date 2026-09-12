package com.example.todo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;

@WebServlet("/lists")
public final class TodoListServlet extends HttpServlet {

    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        Object value = getServletContext().getAttribute(
                "templateEngine"
        );

        if (!(value instanceof TemplateEngine)) {
            throw new ServletException(
                    "TemplateEngine is not initialized"
            );
        }

        templateEngine = (TemplateEngine) value;
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");

        Context context = new Context(request.getLocale());

        templateEngine.process(
                "list",
                context,
                response.getWriter()
        );
    }
}
