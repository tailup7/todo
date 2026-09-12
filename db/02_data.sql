\encoding UTF8

BEGIN;

-- 開発用ユーザー。全ユーザーのパスワードは "password"。
-- 値は BCrypt ハッシュであり、本番用の初期データとしては使用しないこと。
INSERT INTO user_account (name, password_hash)
VALUES
    (
        'tanaka',
        '$2a$12$5dHORiEgeRJCizz.W5adQuCCuV54aF95imAS.KecLE9Bv1J7fdXz6'
    ),
    (
        'suzuki',
        '$2a$12$5dHORiEgeRJCizz.W5adQuCCuV54aF95imAS.KecLE9Bv1J7fdXz6'
    );

-- IDを決め打ちせず、ユーザー名から user_id を取得してリストを作成する。
INSERT INTO todos_list (user_id, list_name)
SELECT
    user_account.id,
    data.list_name
FROM (
    VALUES
        ('tanaka', '仕事'),
        ('tanaka', '個人'),
        ('suzuki', '買い物')
) AS data(user_name, list_name)
JOIN user_account
    ON user_account.name = data.user_name;

-- ユーザー名とリスト名から list_id を取得して Todo を作成する。
INSERT INTO todos (list_id, title, description, status)
SELECT
    todos_list.id,
    data.title,
    data.description,
    data.status
FROM (
    VALUES
        ('tanaka', '仕事', '週次ミーティングの資料を準備する', NULL, 'NOT_STARTED'),
        ('tanaka', '仕事', '見積書を確認する', NULL, 'IN_PROGRESS'),
        ('tanaka', '個人', '図書館の本を返却する', NULL, 'COMPLETED'),
        ('suzuki', '買い物', '牛乳を買う', NULL, 'NOT_STARTED')
) AS data(user_name, list_name, title, description, status)
JOIN user_account
    ON user_account.name = data.user_name
JOIN todos_list
    ON todos_list.user_id = user_account.id
   AND todos_list.list_name = data.list_name;

COMMIT;
