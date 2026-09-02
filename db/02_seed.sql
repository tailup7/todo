\encoding UTF8

INSERT INTO todo (title, description, completed)
VALUES
    (
        'Servletで一覧画面を作る',
        'TodoServlet と list.jsp を接続する',
        FALSE
    ),
    (
        'PostgreSQL接続を確認する',
        'JNDI DataSource 経由で SELECT を実行する',
        TRUE
    );