package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/parkinglotdb";
    private static final String USER = "root";  // Use your MySQL username
    private static final String PASSWORD = "";  // Use your MySQL password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}