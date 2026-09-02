package com.example.todo.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.Base64;

public final class CsrfFilter implements Filter {

    private static final String SESSION_KEY =
            "csrfToken";

    private static final SecureRandom RANDOM =
            new SecureRandom();

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest =
                (HttpServletRequest) request;

        HttpServletResponse httpResponse =
                (HttpServletResponse) response;

        HttpSession session =
                httpRequest.getSession(true);

        String expected =
                (String) session.getAttribute(
                        SESSION_KEY
                );

        if (expected == null) {
            expected = newToken();

            session.setAttribute(
                    SESSION_KEY,
                    expected
            );
        }

        httpRequest.setAttribute(
                "csrfToken",
                expected
        );

        if ("POST".equalsIgnoreCase(
                httpRequest.getMethod())) {

            String actual =
                    httpRequest.getParameter("_csrf");

            if (!constantTimeEquals(
                    expected,
                    actual)) {

                httpResponse.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Invalid CSRF token"
                );

                return;
            }
        }

        chain.doFilter(
                request,
                response
        );
    }

    private static String newToken() {

        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private static boolean constantTimeEquals(
            String expected,
            String actual) {

        if (expected == null || actual == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(
                        StandardCharsets.UTF_8),
                actual.getBytes(
                        StandardCharsets.UTF_8)
        );
    }
}
