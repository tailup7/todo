package com.example.todo.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // false: セッションがなければ新規作成しない
        HttpSession session = httpRequest.getSession(false);

        Object authenticatedUserId =
                session == null
                        ? null
                        : session.getAttribute("authenticatedUserId");

        // AuthServlet が Long 型のユーザー ID を保存していることも確認する
        if (!(authenticatedUserId instanceof Long)) {
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/login"
            );
            return;
        }

        // ログイン済みなら、後続の Filter・Servlet を実行する
        chain.doFilter(request, response);
    }
}