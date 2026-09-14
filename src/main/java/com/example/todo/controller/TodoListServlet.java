package com.example.todo.controller;

import com.example.todo.model.TodoList;
import com.example.todo.repository.RepositoryException;
import com.example.todo.service.TodoListNotFoundException;
import com.example.todo.service.TodoListService;
import com.example.todo.service.ValidationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/lists", "/lists/*"})
public final class TodoListServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication webApplication;
    private TodoListService todoListService;

    @Override
    public void init() throws ServletException {

        Object templateEngineValue = getServletContext().getAttribute("templateEngine");

        Object serviceValue = getServletContext().getAttribute("todoListService");

        if (!(templateEngineValue instanceof TemplateEngine)) {
            throw new ServletException("TemplateEngine is not initialized");
        }

        if (!(serviceValue instanceof TodoListService)) {
            throw new ServletException("TodoListService is not initialized");
        }

        templateEngine = (TemplateEngine) templateEngineValue;

        todoListService = (TodoListService) serviceValue;

        webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String path = normalizePath(request.getPathInfo());

            switch (path) {
                case "/" -> showList(request, response);
                case "/new" -> showCreateForm(request, response);
                case "/edit" -> showEditForm(request, response);
                default -> response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
            }
        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        } catch (TodoListNotFoundException e) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
        } catch (RepositoryException e) {
            handleDatabaseError(e);

            throw new ServletException(
                    "Database operation failed",
                    e
            );
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            String path = normalizePath(request.getPathInfo());

            switch (path) {
                case "/create" -> create(request, response);
                case "/update" -> update(request, response);
                case "/delete" -> delete(request, response);
                default -> response.sendError(
                        HttpServletResponse.SC_NOT_FOUND
                );
            }
        } catch (IllegalArgumentException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        } catch (TodoListNotFoundException e) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );
        } catch (RepositoryException e) {
            handleDatabaseError(e);

            throw new ServletException(
                    "Database operation failed",
                    e
            );
        }
    }

    private void showList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        long userId = getAuthenticatedUserId(request);

        List<TodoList> todoLists =
                todoListService.findByUserId(userId);

        response.setContentType("text/html; charset=UTF-8");

        WebContext context = createWebContext(
                request,
                response
        );

        context.setVariable("todoLists", todoLists);

        context.setVariable(
                "csrfToken",
                request.getAttribute("csrfToken")
        );

        templateEngine.process(
                "list",
                context,
                response.getWriter()
        );
    }

    private void showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        renderForm(
                request,
                response,
                "create",
                0L,
                "",
                null
        );
    }

    private void showEditForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        long userId = getAuthenticatedUserId(request);

        long listId = parseId(request);

        TodoList todoList =
                todoListService.findByIdAndUserId(
                        listId,
                        userId
                );

        renderForm(
                request,
                response,
                "edit",
                todoList.id(),
                todoList.listName(),
                null
        );
    }

    private void create(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        long userId = getAuthenticatedUserId(request);

        String listName =
                request.getParameter("listName");

        try {
            todoListService.create(
                    userId,
                    listName
            );
        } catch (ValidationException e) {
            renderForm(
                    request,
                    response,
                    "create",
                    0L,
                    listName,
                    e.getErrors()
            );
            return;
        }

        redirectToList(request, response);
    }

    private void update(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        long userId = getAuthenticatedUserId(request);

        long listId = parseId(request);

        String listName =
                request.getParameter("listName");

        try {
            todoListService.rename(
                    listId,
                    userId,
                    listName
            );
        } catch (ValidationException e) {
            renderForm(
                    request,
                    response,
                    "edit",
                    listId,
                    listName,
                    e.getErrors()
            );
            return;
        }

        redirectToList(request, response);
    }

    private void delete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        long userId = getAuthenticatedUserId(request);

        long listId = parseId(request);

        todoListService.delete(
                listId,
                userId
        );

        redirectToList(request, response);
    }

    private void renderForm(
            HttpServletRequest request,
            HttpServletResponse response,
            String mode,
            long listId,
            String listName,
            Map<String, String> errors)
            throws IOException {

        response.setContentType("text/html; charset=UTF-8");

        WebContext context = createWebContext(
                request,
                response
        );

        context.setVariable("mode", mode);
        context.setVariable("listId", listId);
        context.setVariable("listName", listName);
        context.setVariable("errors", errors);

        context.setVariable(
                "csrfToken",
                request.getAttribute("csrfToken")
        );

        templateEngine.process(
                "list-form",
                context,
                response.getWriter()
        );
    }

    private WebContext createWebContext(
            HttpServletRequest request,
            HttpServletResponse response) {

        return new WebContext(
                webApplication.buildExchange(
                        request,
                        response
                ),
                request.getLocale()
        );
    }

    private long getAuthenticatedUserId(
            HttpServletRequest request)
            throws ServletException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute(
                        "authenticatedUserId"
                ) instanceof Long userId)) {

            throw new ServletException(
                    "Authenticated user is not available"
            );
        }

        return userId;
    }

    private long parseId(HttpServletRequest request) {

        String value = request.getParameter("id");

        if (value == null) {
            throw new IllegalArgumentException(
                    "id is required"
            );
        }

        try {
            long id = Long.parseLong(value);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "id must be positive"
                );
            }

            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "invalid id",
                    e
            );
        }
    }

    private void redirectToList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/lists"
        );
    }

    private void handleDatabaseError(
            RepositoryException e) {

        getServletContext().log(
                "Todo list database operation failed",
                e
        );
    }

    private String normalizePath(String path) {

        if (path == null
                || path.isBlank()
                || "/".equals(path)) {

            return "/";
        }

        return path;
    }
}
