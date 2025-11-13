package DatabaseConnector;

import Screens.LoginFrame;

import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Manages the initial connection to the MySQL database.
 * This class loads connection parameters from a configuration file, attempts to connect
 * multiple times, and if all attempts fail, it launches the {@code DatabaseChef} setup utility.
 */
public class DatabaseConnector {

    private Connection conn = null;
    private final int maxTries = 4;
    private final Properties config = new Properties();
    private final DatabaseChef dbc;

    private String host;
    private String port;
    private String user;
    private String password;
    private String database;

    private static final String CONFIG_PATH = "config.properties";

    /**
     * Constructs a {@code DatabaseConnector} instance.
     * Loads the configuration and immediately attempts to establish a database connection.
     *
     * @param dbc The instance of {@code DatabaseChef} to be used as a fallback if connection fails.
     */
    public DatabaseConnector(DatabaseChef dbc) {
        this.dbc = dbc;
        loadOrCreateConfig();
        connect();
    }

    /**
     * Loads database configuration properties from the {@value #CONFIG_PATH} file.
     * If the file is missing, default connection parameters are used and a new configuration
     * file is created.
     */
    private void loadOrCreateConfig() {
        File configFile = new File(CONFIG_PATH);

        if (!configFile.exists()) {
            System.out.println(" No config file found. Creating default one...");
            host = "localhost";
            port = "32768";
            user = "root";
            password = "root";
            database = "khalapaqokolo2333533";

            saveConfig();   // save default config
            return;
        }

        try (FileInputStream fis = new FileInputStream(configFile)) {
            config.load(fis);

            host = config.getProperty("db.host", "localhost");
            port = config.getProperty("db.port", "32768");
            user = config.getProperty("db.user", "root");
            password = config.getProperty("db.password", "");
            database = config.getProperty("db.database", "khalapaqokolo2333533");

            System.out.println(" Loaded DB config from file.");

        } catch (IOException e) {
            System.out.println(" Failed to load config file: " + e.getMessage());
        }
    }

    /**
     * Saves the current database connection settings (host, port, user, password, database)
     * back to the configuration file ({@value #CONFIG_PATH}).
     */
    private void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH)) {
            config.setProperty("db.host", host);
            config.setProperty("db.port", port);
            config.setProperty("db.user", user);
            config.setProperty("db.password", password);
            config.setProperty("db.database", database);

            config.store(fos, "Database configuration - Auto generated");
            System.out.println("Saved DB config to file.");
        } catch (IOException e) {
            System.out.println(" Failed to save config file: " + e.getMessage());
        }
    }

    /**
     * Attempts to establish a JDBC connection to the configured database.
     * It retries the connection up to {@code maxTries} times. If successful, it launches
     * the {@code LoginFrame}. If all attempts fail, it calls {@code PULL_OUT_THE_BIG_GUNS()}.
     */
    private void connect() {
        final String url =
                "jdbc:mysql://" + host + ":" + port + "/" + database + "?serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(" JDBC Driver not found: " + e.getMessage());
            return;
        }

        for (int attempt = 1; attempt <= maxTries; attempt++) {
            try {
                System.out.println("Attempting DB connection (" + attempt + "/" + maxTries + ")...");

                conn = DriverManager.getConnection(url, user, password);

                if (conn != null) {
                    System.out.println("Connected to database.");
                    new LoginFrame(conn).setVisible(true);
                    return;
                }
            } catch (SQLException e) {
                System.out.println("Connection failed: " + e.getMessage());
            }
        }

        System.out.println(" All attempts failed. Launching DB config UI...");
        PULL_OUT_THE_BIG_GUNS();
    }

    /**
     * A fallback method that is executed when all connection attempts fail.
     * It launches the {@code DatabaseChef} setup utility to allow the user to manually configure
     * or create the database connection.
     */
    private void PULL_OUT_THE_BIG_GUNS() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            dbc.setVisible(true);
            dbc.log("Connection failed — configure DB settings.");
        });
    }

    /**
     * Returns the established database connection.
     *
     * @return The active {@code Connection} object, or {@code null} if connection failed.
     */
    public Connection getConn() {
        return this.conn;
    }
}