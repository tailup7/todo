// Controllerにあたるクラス。Controllerは、ユーザーのHTTPリクエストを受け取り、適切な処理を呼び出し、HTTPレスポンスを返す役割を持つ。

package com.example.todo.web;

import com.example.todo.dao.TodoDao;
import com.example.todo.model.Todo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.sql.SQLException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

// extendsは、継承を意味する。HttpServletを継承している。
public final class TodoServlet
        extends HttpServlet {
        // メンバ変数todoDaoの宣言
        private TodoDao todoDao;

        // overrideは、親クラス(今回はHttpSevlet)のメソッドを子クラス側で定義しなおすこと。
        @Override
        public void init()
                throws ServletException {

        Object value =
                getServletContext()
                        .getAttribute("todoDao");

        if (!(value instanceof TodoDao)) {
                throw new ServletException(
                        "TodoDao is not initialized"
                );
        }

        this.todoDao = (TodoDao) value;
        }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String path =
                    normalizePath(
                            request.getPathInfo()
                    );

            switch (path) {

                case "/" ->
                        showList(
                                request,
                                response
                        );

                case "/new" ->
                        showCreateForm(
                                request,
                                response
                        );

                case "/edit" ->
                        showEditForm(
                                request,
                                response
                        );

                default ->
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND
                        );
            }

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

            handleDatabaseError(e);

            throw new ServletException(
                    "Database operation failed",
                    e
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        try {
            String path =
                    normalizePath(
                            request.getPathInfo()
                    );

            switch (path) {

                case "/create" ->
                        create(
                                request,
                                response
                        );

                case "/update" ->
                        update(
                                request,
                                response
                        );

                case "/delete" ->
                        delete(
                                request,
                                response
                        );

                default ->
                        response.sendError(
                                HttpServletResponse.SC_NOT_FOUND
                        );
            }

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (SQLException e) {

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
            throws SQLException,
                   ServletException,
                   IOException {

        request.setAttribute(
                "todos",
                todoDao.findAll()
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/list.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    private void showCreateForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
                   IOException {

        request.setAttribute(
                "mode",
                "create"
        );

        request.setAttribute(
                "todo",
                new Todo()
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/form.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    private void showEditForm(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
                   ServletException,
                   IOException {

        long id = parseId(request);

        Optional<Todo> todo =
                todoDao.findById(id);

        if (todo.isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        request.setAttribute(
                "mode",
                "edit"
        );

        request.setAttribute(
                "todo",
                todo.get()
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/form.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    private void create(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
                   ServletException,
                   IOException {

        Todo todo =
                readTodoFromRequest(
                        request,
                        0L
                );

        Map<String, String> errors =
                validate(todo);

        if (!errors.isEmpty()) {

            renderFormWithErrors(
                    request,
                    response,
                    todo,
                    errors,
                    "create"
            );

            return;
        }

        todoDao.create(todo);

        redirectToList(
                request,
                response
        );
    }

    private void update(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
                   ServletException,
                   IOException {

        long id = parseId(request);

        Optional<Todo> existing =
                todoDao.findById(id);

        if (existing.isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        Todo todo =
                readTodoFromRequest(
                        request,
                        id
                );

        todo.setCreatedAt(
                existing.get().getCreatedAt()
        );

        todo.setUpdatedAt(
                existing.get().getUpdatedAt()
        );

        Map<String, String> errors =
                validate(todo);

        if (!errors.isEmpty()) {

            renderFormWithErrors(
                    request,
                    response,
                    todo,
                    errors,
                    "edit"
            );

            return;
        }

        boolean updated =
                todoDao.update(todo);

        if (!updated) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        redirectToList(
                request,
                response
        );
    }

    private void delete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws SQLException,
                   IOException {

        long id = parseId(request);

        boolean deleted =
                todoDao.delete(id);

        if (!deleted) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND
            );

            return;
        }

        redirectToList(
                request,
                response
        );
    }

    private Todo readTodoFromRequest(
            HttpServletRequest request,
            long id) {

        String title =
                trim(
                        request.getParameter(
                                "title"
                        )
                );

        String description =
                trim(
                        request.getParameter(
                                "description"
                        )
                );

        if (description.isEmpty()) {
            description = null;
        }

        boolean completed =
                request.getParameter(
                        "completed"
                ) != null;

        return new Todo(
                id,
                title,
                description,
                completed,
                null,
                null
        );
    }

    private Map<String, String> validate(
            Todo todo) {

        Map<String, String> errors =
                new LinkedHashMap<>();

        if (todo.getTitle() == null
                || todo.getTitle().isBlank()) {

            errors.put(
                    "title",
                    "タイトルは必須です。"
            );

        } else if (
                todo.getTitle().length() > 200) {

            errors.put(
                    "title",
                    "タイトルは200文字以内です。"
            );
        }

        if (todo.getDescription() != null
                && todo.getDescription().length()
                   > 2000) {

            errors.put(
                    "description",
                    "説明は2000文字以内です。"
            );
        }

        return errors;
    }

    private void renderFormWithErrors(
            HttpServletRequest request,
            HttpServletResponse response,
            Todo todo,
            Map<String, String> errors,
            String mode)
            throws ServletException,
                   IOException {

        request.setAttribute(
                "todo",
                todo
        );

        request.setAttribute(
                "errors",
                errors
        );

        request.setAttribute(
                "mode",
                mode
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/form.jsp"
                )
                .forward(
                        request,
                        response
                );
    }

    private long parseId(
            HttpServletRequest request) {

        String value =
                request.getParameter("id");

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
                request.getContextPath()
                        + "/todos"
        );
    }

    private void handleDatabaseError(
            SQLException e) {

        getServletContext().log(
                "Database operation failed",
                e
        );
    }

    private String normalizePath(
            String path) {

        if (path == null
                || path.isBlank()
                || "/".equals(path)) {

            return "/";
        }

        return path;
    }

    private String trim(String value) {

        return value == null
                ? ""
                : value.trim();
    }
}

