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