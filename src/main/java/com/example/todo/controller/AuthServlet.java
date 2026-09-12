package com.example.todo.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.example.todo.model.AuthenticatedUser;
import com.example.todo.service.AuthService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@WebServlet("/login")
public final class AuthServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        Object templateEngineValue = getServletContext().getAttribute("templateEngine");

        if (!(templateEngineValue instanceof TemplateEngine)) {
            throw new ServletException("TemplateEngine is not initialized");
        }

        Object authServiceValue = getServletContext().getAttribute("authService");

        if (!(authServiceValue instanceof AuthService)) {
            throw new ServletException("AuthService is not initialized");
        }

        templateEngine = (TemplateEngine) templateEngineValue;
        authService = (AuthService) authServiceValue;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        renderLogin(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String name = request.getParameter("username");
        String password = request.getParameter("password");

        char[] rawPassword =
                password == null
                        ? null
                        : password.toCharArray();

        Optional<AuthenticatedUser> authenticatedUser;

        try {
            authenticatedUser = authService.authenticate(name, rawPassword);
        } finally {
            if (rawPassword != null) {
                Arrays.fill(rawPassword, '\0');
            }
        }

        if (authenticatedUser.isEmpty()) {
            renderLogin(
                    request,
                    response,
                    "ユーザー名またはパスワードが違います。"
            );
            return;
        }

        HttpSession session = request.getSession(true);

        request.changeSessionId();

        session.setAttribute("authenticatedUserId", authenticatedUser.get().id());

        response.sendRedirect(request.getContextPath() + "/lists");
    }

    private void renderLogin(HttpServletRequest request, HttpServletResponse response, String error)
        throws IOException {

        response.setContentType("text/html; charset=UTF-8");
        Context context = new Context(request.getLocale());
        context.setVariable("username", request.getParameter("username"));
        context.setVariable("error", error);
        context.setVariable("csrfToken", request.getAttribute("csrfToken"));
        templateEngine.process("login", context, response.getWriter());
    }
}
