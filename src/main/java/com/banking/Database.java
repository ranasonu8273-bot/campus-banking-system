package com.banking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Central JDBC connection provider. Configure credentials through environment variables. */
public final class Database {
    private static final String URL = System.getenv().getOrDefault(
            "BANK_DB_URL", "jdbc:mysql://localhost:3306/banking_system");
    private static final String USER = System.getenv().getOrDefault("BANK_DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("BANK_DB_PASSWORD", "");

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
