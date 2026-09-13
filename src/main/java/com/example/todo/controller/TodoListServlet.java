package com.example.todo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.example.todo.model.TodoList;
import com.example.todo.repository.TodoListRepository;

import java.io.IOException;
import java.util.List;

@WebServlet("/lists")
public final class TodoListServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication webApplication;
    private TodoListRepository todoListRepository;

    @Override
    public void init() throws ServletException {
        Object value = getServletContext().getAttribute("templateEngine");
        Object repositoryValue = getServletContext().getAttribute("todoListRepository");

        if (!(value instanceof TemplateEngine)) {
            throw new ServletException("TemplateEngine is not initialized");
        }

        if (!(repositoryValue instanceof TodoListRepository)) {
        throw new ServletException("TodoListRepository is not initialized");
        }

        templateEngine = (TemplateEngine) value;
        todoListRepository = (TodoListRepository) repositoryValue;
        webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        response.setContentType("text/html; charset=UTF-8");
        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("authenticatedUserId")
                        instanceof Long userId)) {
            throw new ServletException("Authenticated user is not available");
        }

        List<TodoList> todoLists = todoListRepository.findByUserId(userId);

        WebContext context = new WebContext(webApplication.buildExchange(request, response), request.getLocale());

        context.setVariable("todoLists", todoLists);
        context.setVariable("csrfToken", request.getAttribute("csrfToken"));

        templateEngine.process(
                "list",
                context,
                response.getWriter()
        );
    }
}
