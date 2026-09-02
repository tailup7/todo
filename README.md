# directory structure

```
todo/
  ├─ pom.xml
  ├─ .gitignore
  ├─ README.md
  ├─ db/
  │     ├─ 00_database.psql
  │     ├─ 01_schema.sql
  │     └─ 02_seed.sql
  ├─ src/
  │     ├─ main/
  │     │     ├─ java/
  │     │     │     └─ com/
  │     │     │           └─ example/
  │     │     │                 └─ todo/
  │     │     │                       ├─ model/                          # Model
  │     │     │                       │     └─ Todo.java
  │     │     │                       ├─ dao/                            # データアクセス層
  │     │     │                       │     ├─ TodoDao.java   
  │     │     │                       │     └─ JdbcTodoDao.java
  │     │     │                       ├─ db/                             # DB接続基盤
  │     │     │                       │     ├─ ConnectionProvider.java   
  │     │     │                       │     └─ Database.java             
  │     │     │                       └─ web/
  │     │     │                             ├─ AppContextListener.java
  │     │     │                             ├─ CsrfFilter.java
  │     │     │                             └─ TodoServlet.java          # Controller
  │     │     ├─ resources/
  │     │     │     └─ logging.properties
  │     │     └─ webapp/
  │     │           └─ WEB-INF/
  │     │                 ├─ web.xml
  │     │                 └─ views/                                      # View
  │     │                       ├─ list.jsp
  │     │                       ├─ form.jsp
  │     │                       └─ error.jsp
  │     └─ test/
  │           └─ java/
  └─ target/                                                             # Maven生成。Gitには含めない
```

#

## DB接続方法
このアプリケーションでは、PostgreSQLへの接続方法として`direct`と`jndi`の2種類を利用できる。

- 方法1: direct
    - DriverManagerを使用してPostgreSQLへ直接接続する
        - PowerShellで以下の環境変数を設定する
          ```
            $env:TODO_DB_MODE = "direct"
            $env:TODO_DB_URL = "jdbc:postgresql://127.0.0.1:5432/todo"
            $env:TODO_DB_USER = "todo_app"
            $env:TODO_DB_PASSWORD = "<todo_appユーザに設定した実際のパスワード>"
          ```
      directモードでTomcatを起動する場合は、上記環境変数を設定したPowerShellからTomcatを起動する。
      ```powershell
        ├─ TODO_DB_MODE 
        ├─ TODO_DB_URL 
        ├─ TODO_DB_USER 
        └─ TODO_DB_PASSWORD 
                │ 
                ▼ 
              Tomcat 
                │ 
                ▼ 
              Database.java 
                │ 
                ▼ 
              DriverManager 
                │ 
                ▼ 
              PostgreSQL
      ```

- 方法2: jndi
    - TomcatのJNDI DataSourceを使用してPostgreSQLへ接続する
    - TODO_DB_* は不要
    - Tomcat側に jdbc/TodoDB を設定