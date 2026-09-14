// Springにおける @configuration クラス + @Bean メソッド + アプリ起動時の初期化処理 をまとめて手書きしているようなファイル

package com.example.todo.config;

import com.example.todo.database.ConnectionProvider;
import com.example.todo.database.Database;
import com.example.todo.repository.JdbcTodoListRepository;
import com.example.todo.repository.JdbcTodoRepository;
import com.example.todo.repository.JdbcUserAccountRepository;
import com.example.todo.repository.TodoListRepository;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserAccountRepository;
import com.example.todo.security.BCryptPasswordHasher;
import com.example.todo.security.PasswordHasher;
import com.example.todo.service.AuthService;
import com.example.todo.service.AuthServiceImpl;
import com.example.todo.service.TodoListService;
import com.example.todo.service.TodoListServiceImpl;
import com.example.todo.service.TodoService;
import com.example.todo.service.TodoServiceImpl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

public final class AppContextListener implements ServletContextListener {
        @Override
        public void contextInitialized(ServletContextEvent event) {
                ServletContext context = event.getServletContext();
                try {
                        ConnectionProvider connectionProvider =Database.create(context);
                        TodoListRepository todoListRepository = new JdbcTodoListRepository(connectionProvider);
                        TodoListService todoListService = new TodoListServiceImpl(todoListRepository);
                        TodoRepository todoRepository = new JdbcTodoRepository(connectionProvider);
                        TodoService todoService = new TodoServiceImpl(todoRepository);
                        UserAccountRepository userAccountRepository = new JdbcUserAccountRepository(connectionProvider);
                        PasswordHasher passwordHasher = new BCryptPasswordHasher();
                        AuthService authService = new AuthServiceImpl(userAccountRepository, passwordHasher);

                        context.setAttribute("todoService", todoService);
                        context.setAttribute("todoListRepository", todoListRepository);
                        context.setAttribute("todoListService", todoListService);
                        context.setAttribute("authService", authService);
                        context.setAttribute("templateEngine", createTemplateEngine(context));
                        context.log("Todo application initialized");
                } catch (RuntimeException e) {
                        context.log("Todo application initialization failed",e);
                        throw e;
                }
        }

        private TemplateEngine createTemplateEngine(ServletContext servletContext) {
                IWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
                WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);

                templateResolver.setPrefix("/WEB-INF/templates/");
                templateResolver.setSuffix(".html");
                templateResolver.setTemplateMode(TemplateMode.HTML);
                templateResolver.setCharacterEncoding("UTF-8");
                templateResolver.setCacheable(true);

                TemplateEngine templateEngine = new TemplateEngine();
                templateEngine.setTemplateResolver(templateResolver);

                return templateEngine;
        }
}
