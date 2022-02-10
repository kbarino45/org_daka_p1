package com.revature.daka.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcConnection {

    // Create a java.sql.Connection object from JDBC.
    private static Connection conn = null;

    public static Connection getConnection() {
        if (conn == null) {
            // Establish a new connection. Load endpoint address, username and PW from a file.
            Properties props = new Properties();

            try {
                Class.forName("org.postgresql.Driver");
                props.load(JdbcConnection.class.getClassLoader().getResourceAsStream("connection.properties"));

                String endpoint = props.getProperty("endpoint");
                String username = props.getProperty("username");
                String password = props.getProperty("password");
                String url = "jdbc:postgresql://" + endpoint + "/postgres";

                conn = DriverManager.getConnection(url, username, password);

            } catch (IOException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }
}

