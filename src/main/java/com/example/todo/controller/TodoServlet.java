// Controllerにあたるクラス。Controllerは、ユーザーのHTTPリクエストを受け取り、適切な処理を呼び出し、HTTPレスポンスを返す役割を持つ。

package com.example.todo.controller;

import com.example.todo.model.Todo;
import com.example.todo.repository.RepositoryException;
import com.example.todo.service.TodoNotFoundException;
import com.example.todo.service.TodoService;
import com.example.todo.service.ValidationException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.util.Map;

// @WebServletアノテーションは、TodoServletとURL(/todos/*)をマッピング
// /todos ではなく /todos/* なのは、/todos配下の複数のURLをこのTodoServletに処理させるため。
@WebServlet(urlPatterns = "/todos/*")
// extendsは、継承を意味する。HttpServletを継承している。
public final class TodoServlet extends HttpServlet {
        // メンバ変数todoServiceの宣言
        private TodoService todoService;
        // overrideは、親クラス(今回はHttpSevlet)のメソッドを子クラス側で定義しなおすこと。
        @Override
        // initメソッド : Tomcatによって生成されたServletを、利用可能な状態に初期化する。
        public void init() throws ServletException {
        // getServletContext()は、webアプリケーション全体で共有される`ServletContext`を取得するメソッド。
        // ServletContextには、アプリケーション全体で共有したい情報を`attribute`として保存できる。
                Object value = getServletContext().getAttribute("todoService");
        // 右辺でやっていることは、ServletContextに`todoService`という名前で保存されているオブジェクトを取得している
        // ちなみに、servletContext.setAttribute("todoService", todoService);という処理は、AppContextListener.javaの
        // contextInitializedメソッドで行われている。
        // つまり、TodoServletのinit()メソッドが実行される時点までに、AppContextListenerのcontextInitializedメソッドが実行されているので、
        // todoServiceはすでにServletContextに保存されている。
        // 典型的な流れは以下のようになる。
        // TomcatがWebアプリを起動
        //         ↓
        // AppContextListener生成
        //         ↓
        // contextInitialized() 実行
        //         ↓
        // TodoRepositoryとTodoServiceを生成
        //         ↓
        // ServletContext.setAttribute(
        //     "todoService", todoService
        // )
        //         ↓
        // TodoServlet生成・初期化
        //         ↓
        // TodoServlet.init() 実行
        //         ↓
        // getAttribute("todoService")
        //         ↓
        // this.todoService にセット

                if (!(value instanceof TodoService)) {
                        throw new ServletException("TodoService is not initialized");
                }
                this.todoService = (TodoService) value;
        }

        // GETメソッド。
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {
                try {
                String path = normalizePath(request.getPathInfo());
                switch (path) {
                        case "/" -> showList(request, response);
                        case "/new" -> showCreateForm(request, response);
                        case "/edit" -> showEditForm(request, response);
                        default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        }
                } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                } catch (TodoNotFoundException e) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } catch (RepositoryException e) {
                handleDatabaseError(e);
                throw new ServletException("Database operation failed", e);
                }
        }

        // POSTメソッド
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

        } catch (TodoNotFoundException e) {

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

        // "/todos"にアクセスしたときに、todoの一覧を表示するためのメソッド
        private void showList(HttpServletRequest request,HttpServletResponse response)
                throws ServletException, IOException {
                request.setAttribute("todos", todoService.findAll());
                request.getRequestDispatcher("/WEB-INF/views/list.jsp").forward(request, response);
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
            throws ServletException,
                   IOException {

        long id = parseId(request);

        Todo todo = todoService.findById(id);

        request.setAttribute(
                "mode",
                "edit"
        );

        request.setAttribute(
                "todo",
                todo
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
            throws ServletException,
                   IOException {

        Todo todo =
                readTodoFromRequest(
                        request,
                        0L
                );

        try {
            todoService.create(todo);
        } catch (ValidationException e) {
            renderFormWithErrors(
                    request, response, todo, e.getErrors(), "create");
            return;
        }

        redirectToList(
                request,
                response
        );
    }

    private void update(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException,
                   IOException {

        long id = parseId(request);

        Todo todo =
                readTodoFromRequest(
                        request,
                        id
                );

        try {
            todoService.update(id, todo);
        } catch (ValidationException e) {
            renderFormWithErrors(
                    request, response, todo, e.getErrors(), "edit");
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
            throws IOException {

        long id = parseId(request);

        todoService.delete(id);

        redirectToList(
                request,
                response
        );
    }

        // エラー解消のために、追加したメソッド。なので後で削除するか消す。
        private long parseListId(HttpServletRequest request) {
            String value = request.getParameter("listId");

            if (value == null) {
                throw new IllegalArgumentException("listId is required");
            }

            try {
                long listId = Long.parseLong(value);

                if (listId <= 0) {
                    throw new IllegalArgumentException(
                            "listId must be positive"
                    );
                }

                return listId;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid listId", e);
            }
        }

        private Todo.Status parseStatus(HttpServletRequest request) {
                String value = request.getParameter("status");
                if (value == null) {
                        throw new IllegalArgumentException("status is required");
                }
                try {
                        return Todo.Status.valueOf(value);
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("invalid status",e);
                }
        }

        // todo画面の「新規」ボタンから、todoが作成されてHTTPリクエストを受け取ったとき、
        // java側でもtodoを作成するために、HTTPリクエストのパラメータからtodoの情報を取得する。
        private Todo readTodoFromRequest(HttpServletRequest request, long id) {

                long listId = parseListId(request);
                String title = trim(request.getParameter("title"));
                String description =trim(request.getParameter("description"));
                if (description.isEmpty()) {
                        description = null;
                }
                Todo.Status status = parseStatus(request);

                return new Todo(
                        id,
                        listId,
                        title,
                        description,
                        status,
                        null,
                        null
                );
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

        private long parseId(HttpServletRequest request) {
                String value = request.getParameter("id");
                if (value == null) {
                        throw new IllegalArgumentException("id is required");
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
            RepositoryException e) {

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

