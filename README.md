# directory structure

todo/
├── pom.xml
├── .gitignore
├── README.md
├── db/
│   ├── 00_database.psql
│   ├── 01_schema.sql
│   └── 02_seed.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── example/
│   │   │           └── todo/
│   │   │               ├── model/
│   │   │               │   └── Todo.java
│   │   │               ├── dao/
│   │   │               │   ├── TodoDao.java
│   │   │               │   └── JdbcTodoDao.java
│   │   │               ├── db/
│   │   │               │   ├── ConnectionProvider.java
│   │   │               │   └── Database.java
│   │   │               └── web/
│   │   │                   ├── AppContextListener.java
│   │   │                   ├── CsrfFilter.java
│   │   │                   └── TodoServlet.java
│   │   ├── resources/
│   │   │   └── logging.properties
│   │   └── webapp/
│   │       └── WEB-INF/
│   │           ├── web.xml
│   │           └── views/
│   │               ├── list.jsp
│   │               ├── form.jsp
│   │               └── error.jsp
│   └── test/
│       └── java/
└── target/                  # Maven生成。Gitには含めない
