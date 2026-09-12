# directory structure

```
todo/
  ├─ pom.xml
  ├─ .gitignore
  ├─ README.md
  ├─ db/                                                  # DB作成・スキーマ・初期データ
  │     ├─ 00_database.psql
  │     ├─ 01_schema.sql
  │     └─ 02_data.sql
  ├─ src/
  │     ├─ main/
  │     │     ├─ java/
  │     │     │     └─ com/
  │     │     │           └─ example/
  │     │     │                 └─ todo/
  │     │     │                       ├─ model/                          # ドメインモデル
  │     │     │                       │     └─ Todo.java
  │     │     │                       ├─ controller/                     # Controller
  │     │     │                       │     └─ TodoServlet.java
  │     │     │                       ├─ service/                        # 業務処理・入力検証
  │     │     │                       │     ├─ TodoService.java
  │     │     │                       │     ├─ TodoServiceImpl.java
  │     │     │                       │     ├─ TodoNotFoundException.java
  │     │     │                       │     └─ ValidationException.java
  │     │     │                       ├─ repository/                     # データアクセス層
  │     │     │                       │     ├─ TodoRepository.java
  │     │     │                       │     ├─ JdbcTodoRepository.java
  │     │     │                       │     └─ RepositoryException.java
  │     │     │                       ├─ database/                       # DB接続基盤
  │     │     │                       │     ├─ ConnectionProvider.java
  │     │     │                       │     └─ Database.java
  │     │     │                       ├─ config/                         # 依存関係の組み立て
  │     │     │                       │     └─ AppContextListener.java
  │     │     │                       └─ web/
  │     │     │                             └─ CsrfFilter.java
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

# 準備

## 環境構築
省略

## postgreSQLのロール・DB作成・スキーマ作成・初期データ投入
この手順は1度だけ行う。

1. PostgreSQLのロールとDBの作成

    PostgreSQLの管理ユーザで、プロジェクト直下から以下を実行

    ``` powershell
    psql -U postgres -d postgres -f db\00_database.psql
    ```
    `todo_app` ロール用のパスワード設定を求められるので、入力する。`todo` というデータベースが作られる。

2. テーブルと初期データ作成

    以下のコマンドを実行 (`todo_app` ロールのパスワード入力を求められるので、先ほど設定したパスワードを入力)

    ``` powershell
    psql -U todo_app -d todo -f db\01_schema.sql
    psql -U todo_app -d todo -f db\02_data.sql
    ```

## DB接続
このアプリケーションでは、PostgreSQLへの接続方法として`direct`と`jndi`の2種類を利用できる。

- 方法1: direct
    1. DriverManagerを使用してPostgreSQLへ直接接続する
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

    2. PostgreSQL JDBC 
        
        JNDI DataSourceはTomcat側で生成されるため、TomcatがPostgreSQL JDBC Driverを利用できるようにする。

        (powershellの場合)`pom.xml`がある階層で、以下のコマンドを実行し、MavenにPostgreSQL JDBC Driverを取得させる。

        ```powershell
        mvn dependency:resolve 
        ```

        これにより、ローカルのMavenリポジトリ、すなわち
        ```
        C:\Users\<ユーザー名>\.m2\repository\
          └── org\
            └── postgresql\
                └── postgresql\
                    └── 42.7.13\
                        └── postgresql-42.7.13.jar
        ```
        のような場所に、PostgreSQL JDBC Driver が入る。

        <br>
        その後、取得したJARを Tomcatの `lib` ディレクトリへコピーする。

        ``` powershell
        Copy-Item `
          "$HOME\.m2\repository\org\postgresql\postgresql\42.7.13\postgresql-42.7.13.jar" `
          "$env:CATALINA_HOME\lib\"
        ```

- 方法2: jndi
  - 最初にTomcat環境を構築する際に、一度だけ配置する(JavaソースやJSPを更新してWARを再デプロイしても、DB接続設定を変更しない限り、todo.xmlの修正・再配置は不要)。
  - TomcatのJNDI DataSourceを使用してPostgreSQLへ接続する
  - `TODO_DB_*` 環境変数は不要
  - JNDI方式を利用する場合は、以下の設定を行う。
    - 1: TomcatにJNDI DataSourceを設定

      ```
      $CATALINA_BASE/   # あるいは、$CATALINA_HOME
      └── conf/
          └── Catalina/
              └── localhost/
      ```

      この位置に以下の内容の`todo.xml` を配置する(`password`は適切に書き換えること)。`todo.xml` は、Tomcatに `jdbc/TodoDB` というJNDI DataSourceを登録するための設定ファイルである。

      ```
      <?xml version="1.0" encoding="UTF-8"?>

      <Context>

          <Resource
              name="jdbc/TodoDB"
              auth="Container"
              type="javax.sql.DataSource"

              driverClassName="org.postgresql.Driver"

              url="jdbc:postgresql://127.0.0.1:5432/todo"

              username="todo_app"
              password="<todo_appユーザに設定した実際のパスワード>"

              maxTotal="20"
              maxIdle="10"
              maxWaitMillis="10000"
          />

      </Context>
      ```

      上記のファイルにおいて、`name="jdbc/TodoDB"` の部分は、アプリケーション側でJNDI DataSourceを取得する際に使用する名前と一致させる。
   
    - 2: PostgreSQL JDBC DriverをTomcatのlibへ配置

      JNDI DataSourceはTomcat側で生成されるため、
      TomcatがPostgreSQL JDBC Driverを利用できるようにする。

      (powershellの場合)`pom.xml`がある階層で、以下のコマンドを実行し、MavenにPostgreSQL JDBC Driverを取得させる。

      ```powershell
      mvn dependency:resolve 
      ```
      これにより、ローカルのMavenリポジトリ、すなわち
      ```
      C:\Users\<ユーザー名>\.m2\repository\
      └── org\
          └── postgresql\
              └── postgresql\
                  └── 42.7.13\
                      └── postgresql-42.7.13.jar
      ```
      のような場所に、PostgreSQL JDBC Driver が入る。

      <br>
      その後、取得したJARを Tomcatの `lib` ディレクトリへコピーする。
      ``` powershell
      Copy-Item `
        "$HOME\.m2\repository\org\postgresql\postgresql\42.7.13\postgresql-42.7.13.jar" `
        "$env:CATALINA_HOME\lib\"
      ```
    - Tomcatを再起動する


# ビルド・手動デプロイ

1. ローカルビルド

    `pom.xml`のあるディレクトリで以下のコマンドを実行
    ``` powershell
    mvn clean package
    ```

    `target/`に`todo.war`が生成される。

2. 手動デプロイ

    Tomcatは停止しておく

    ``` powershell
    & "$env:CATALINA_HOME\bin\shutdown.bat"
    ```

    以下のコマンドを実行する

    ``` powershell
    Remove-Item `
      "$env:CATALINA_BASE\webapps\todo.war" `
      -Force `
      -ErrorAction SilentlyContinue

    Remove-Item `
      "$env:CATALINA_BASE\webapps\todo" `
      -Recurse `
      -Force `
      -ErrorAction SilentlyContinue

    Copy-Item `
      "target\todo.war" `
      "$env:CATALINA_BASE\webapps\todo.war"
    ```

    やっていることは、生成された `target\todo.war`を `$CATALINA_BASE\webapps\`に配置しているだけ。コマンド実行ではなくコピペでも可。

3. Tomcatを起動する

    以下のコマンドを実行

    ``` powershell
    & "$env:CATALINA_HOME\bin\startup.bat"
    ```

4. 動作確認

   例えばTomcatのポート番号が8080なら、

   ``` 
   http://localhost:8080/todo/todos
   ```

   にブラウザからアクセスする。
