// DB接続を担う。DBのテーブル変更があっても、このファイルは変更不要。 

package com.example.todo.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
