// Controllerにあたるクラス。Controllerは、ユーザーのHTTPリクエストを受け取り、適切な処理を呼び出し、HTTPレスポンスを返す役割を持つ。

package com.example.todo.controller;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import com.example.todo.model.Todo;
import com.example.todo.model.TodoList;
import com.example.todo.repository.RepositoryException;
import com.example.todo.service.TodoNotFoundException;
import com.example.todo.service.TodoService;
import com.example.todo.service.ValidationException;
import com.example.todo.repository.TodoListRepository;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.util.Map;

// @WebServletアノテーションは、TodoServletとURL(/todos/*)をマッピング
// /todos ではなく /todos/* なのは、/todos配下の複数のURLをこのTodoServletに処理させるため。
@WebServlet(urlPatterns = "/todos/*")
// extendsは、継承を意味する。HttpServletを継承している。
public final class TodoServlet extends HttpServlet {

        private TodoService todoService;
        private TodoListRepository todoListRepository;
        private TemplateEngine templateEngine;
        private JakartaServletWebApplication webApplication;

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

                Object listRepositoryValue = getServletContext().getAttribute("todoListRepository");

                if (!(listRepositoryValue instanceof TodoListRepository)) {
                        throw new ServletException("TodoListRepository is not initialized");
                }

                todoListRepository = (TodoListRepository) listRepositoryValue;

                Object templateEngineValue = getServletContext().getAttribute("templateEngine");

                if (!(templateEngineValue instanceof TemplateEngine)) {
                        throw new ServletException("TemplateEngine is not initialized");
                }

                templateEngine = (TemplateEngine) templateEngineValue;
                webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
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
                        // TodoServlet.javaにて定義されているメソッド。
                long userId = getAuthenticatedUserId(request); //セッションからログインユーザを取得
                long listId = parseListId(request);
                TodoList todoList = todoListRepository.findByIdAndUserId(listId, userId).orElse(null);

                if (todoList == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                response.setContentType("text/html; charset=UTF-8");
                // WebContextは、Thymeleafのテンプレートエンジンに渡すためのコンテキスト情報を保持するクラス。
                WebContext context = new WebContext(webApplication.buildExchange(request, response), request.getLocale());
                context.setVariable("todos", todoService.findByListIdAndUserId(listId, userId));
                context.setVariable("listId", listId);
                context.setVariable("todoList", todoList);
                context.setVariable("csrfToken", request.getAttribute("csrfToken"));
                templateEngine.process("todos", context, response.getWriter());
        }
        
        // Todoの「新規作成画面」を表示するためのメソッド
        private void showCreateForm(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

                long userId = getAuthenticatedUserId(request);
                long listId = parseListId(request);

                if (!todoListRepository.existsByIdAndUserId(listId, userId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }
                Todo todo = new Todo();
                todo.setListId(listId);
                renderForm(request, response, todo, null, "create");
        }

        // Todoの「編集画面」を表示するためのメソッド
        private void showEditForm(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

                long userId = getAuthenticatedUserId(request);
                long id = parseId(request);
                Todo todo = todoService.findById(id);

                if (!todoListRepository.existsByIdAndUserId(todo.getListId(), userId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }
                renderForm(request, response, todo, null, "edit");
        }

        // Todoの新規作成処理を行うためのメソッド
        private void create(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

                long userId = getAuthenticatedUserId(request);
                long listId = parseListId(request);

                if (!todoListRepository.existsByIdAndUserId(listId, userId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }

                Todo todo = readTodoFromRequest(request, 0L, listId);

                try {
                        todoService.create(todo);
                } catch (ValidationException e) {
                        renderFormWithErrors(
                        request, response, todo, e.getErrors(), "create");
                return;
                }

                redirectToList(request, response, listId);
        }

    // Todoの更新処理を行うためのメソッド
        private void update(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

                long userId = getAuthenticatedUserId(request);
                long id = parseId(request);
                // リクエスト値ではなく、DB にある Todo を取得する
                Todo existing = todoService.findById(id);
                // DB 上の実際の listId を使って所有者確認する
                if (!todoListRepository.existsByIdAndUserId(existing.getListId(), userId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }
                Todo todo = readTodoFromRequest(request, id, existing.getListId());

                try {
                        todoService.update(id, todo);
                } catch (ValidationException e) {
                        renderFormWithErrors(request, response, todo, e.getErrors(), "edit");
                        return;
                }

                redirectToList(request, response, existing.getListId());
        }

        // Todoの削除処理を行うためのメソッド
        private void delete(HttpServletRequest request, HttpServletResponse response)
                throws ServletException, IOException {

                long userId = getAuthenticatedUserId(request);
                long id = parseId(request);
                Todo existing = todoService.findById(id);
                if (!todoListRepository.existsByIdAndUserId(existing.getListId(), userId)) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        return;
                }
                todoService.delete(id);
                redirectToList(request, response, existing.getListId());
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
        private Todo readTodoFromRequest(HttpServletRequest request, long id, long listId) {

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

        private void renderForm(
                HttpServletRequest request,
                HttpServletResponse response,
                Todo todo,
                Map<String, String> errors,
                String mode)
                throws IOException {

                response.setContentType("text/html; charset=UTF-8");
                WebContext context = new WebContext(webApplication.buildExchange(request, response), request.getLocale());

                context.setVariable("mode", mode);
                context.setVariable("todo", todo);
                context.setVariable("errors", errors);
                // Todo.Status.values() を渡すので、プルダウンが生成される
                context.setVariable("statuses", Todo.Status.values());
                context.setVariable("csrfToken", request.getAttribute("csrfToken"));
                templateEngine.process("form", context, response.getWriter());
        }

        private void renderFormWithErrors(
                HttpServletRequest request,
                HttpServletResponse response,
                Todo todo,
                Map<String, String> errors,
                String mode)
                throws IOException {
                renderForm(request, response, todo, errors, mode);
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

        // Todoの一覧画面にリダイレクトするためのメソッド
        private void redirectToList(HttpServletRequest request, HttpServletResponse response, long listId)
                throws IOException {
        response.sendRedirect(request.getContextPath() + "/todos?listId=" + listId);
        }

    private void handleDatabaseError(
            RepositoryException e) {

        getServletContext().log(
                "Database operation failed",
                e
        );
    }

        // パスを正規化する補助メソッド。
        private String normalizePath(String path) {
                if (path == null || path.isBlank() || "/".equals(path)) {
                        return "/";
                } 
                return path;
        }

        // 文字列の前後の空白を削除する補助メソッド。
        private String trim(String value) {
        return value == null
                ? ""
                : value.trim();
        }

        // showListメソッドで使う補助メソッド。
        private long getAuthenticatedUserId(HttpServletRequest request)
        throws ServletException {
                HttpSession session = request.getSession(false);
                if (session == null || !(session.getAttribute("authenticatedUserId") instanceof Long userId)) {
                        throw new ServletException("Authenticated user is not available");
                }
                return userId;
        }

}

