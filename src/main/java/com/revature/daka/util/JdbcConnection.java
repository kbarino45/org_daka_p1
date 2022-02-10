package com.revature.daka.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcConnection {

    private static Connection connection = null;

    /**
     * Establishes a connection if none exists. Returns the current connection otherwise.
     * @return Connection
     */
    public static Connection getConnection() {

        if (connection == null) {

            Properties props = new Properties();
            try {
                Class.forName("org.postgresql.Driver");
                props.load(JdbcConnection.class.getClassLoader().getResourceAsStream("connection.properties"));

                String endpoint = props.getProperty("endpoint");
                String url = "jdbc:postgresql://" + endpoint + "/postgres";
                String username = props.getProperty("username");
                String password = props.getProperty("password");

                connection = DriverManager.getConnection(url, username, password);

            } catch (IOException | SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } //end if
        Logger.logger.info("JDBC Connection Established");
        return connection;
    }
}
