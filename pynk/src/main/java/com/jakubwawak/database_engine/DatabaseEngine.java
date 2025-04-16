/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.database_engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.jakubwawak.maintanance.ConsoleColors;

/**
 * Database engine for the application
 */
public class DatabaseEngine {

    public boolean connected = false;
    private Connection connection;
    private String databasePath;

    /**
     * Constructor to initialize the database path and connect
     * 
     * @param databasePath
     */
    public DatabaseEngine(String databasePath) {
        this.databasePath = databasePath;
        connect();
    }

    /**
     * Method to connect to the SQLite database
     */
    public void connect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            createDatabase();
            connected = true;
        } catch (SQLException e) {
            connected = false;
            e.printStackTrace();
        }
    }

    /**
     * Method to get the database path
     * 
     * @return databasePath
     */
    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * Method to get the database connection
     * 
     * @return connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Method to execute a SQL query for table creation
     * 
     * @param sql
     * @return number of rows affected
     */
    public int executeSQL(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            return stmt.getUpdateCount();
        } catch (SQLException e) {
            System.out.println("Error executing SQL: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Method to create a host entry in the database
     * 
     * @param hostName
     * @param hostIp
     * @param hostCategory
     * @param hostDescription
     */
    private void createHostEntry(String hostName, String hostIp, String hostCategory, String hostDescription) {
        executeSQL("INSERT INTO host_data (host_name, host_ip, host_category, host_description) VALUES ('" + hostName + "', '" + hostIp + "', '" + hostCategory + "', '" + hostDescription + "');");
    }

    /**
     * Method to create the database
     */
    private void createDatabase() {
        System.out.println("Creating/Checking database");

        if (!doesTableExist("app_log")) {
            System.out.println("Creating app_log table");
            executeSQL("CREATE TABLE app_log (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "host_id INT, " +
                    "log_timestamp TIMESTAMP, " +
                    "log_category VARCHAR(100), " +
                    "log_data TEXT, " +
                    "log_code VARCHAR(20), " +
                    "log_color_hex VARCHAR(20));");
        }

        if (!doesTableExist("host_data")) {
            System.out.println("Creating host_data table");
            executeSQL("CREATE TABLE host_data (" +
                    "host_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "host_name VARCHAR(100), " +
                    "host_ip VARCHAR(100), " +
                    "host_category VARCHAR(100), " +
                    "host_description TEXT);");

            createHostEntry("localhost", "127.0.0.1", "local", "Localhost");
            addLog("info", "Localhost added", "info", ConsoleColors.GREEN_BOLD);
            createHostEntry("google.com", "8.8.8.8", "public", "Google DNS");
            addLog("info", "Google DNS added", "info", ConsoleColors.GREEN_BOLD);
            createHostEntry("cloudflare.com", "1.1.1.1", "public", "Cloudflare DNS");
            addLog("info", "Cloudflare DNS added", "info", ConsoleColors.GREEN_BOLD);
        }

        if (!doesTableExist("ping_history")) {
            System.out.println("Creating ping_history table");
            executeSQL("CREATE TABLE ping_history (" +
                    "ping_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "host_id INT, " +
                    "ping_timestamp TIMESTAMP, " +
                    "packet_status_code VARCHAR(20), " +
                    "packet_status_color_hex VARCHAR(10), " +
                    "packet_transmitted INT, " +
                    "packet_received INT, " +
                    "packet_hop_time1 DOUBLE, " +
                    "packet_hop_time2 DOUBLE, " +
                    "packet_hop_time3 DOUBLE, " +
                    "packet_hop_time4 DOUBLE, " +
                    "packet_hop_time5 DOUBLE, " +
                    "packet_hop_time6 DOUBLE, " +
                    "packet_hop_time7 DOUBLE, " +
                    "packet_hop_time8 DOUBLE, " +
                    "packet_round_trip_time_min DOUBLE, " +
                    "packet_round_trip_time_max DOUBLE, " +
                    "packet_round_trip_time_avg DOUBLE, " +
                    "packet_dig_data TEXT, " +
                    "packet_tracert_data TEXT);");
        }


    }

    /**
     * Method to check if a table exists in the database
     * 
     * @param tableName
     * @return true if the table exists, false otherwise
     */
    private boolean doesTableExist(String tableName) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeQuery(sql).next();
        } catch (SQLException e) {
            System.err.println("Error checking if table exists: " + e.getMessage());
            return false;
        }
    }

    /**
     * Method to add a log to the database
     * 
     * @param category
     * @param data
     * @param code
     * @param colorHex
     */
    public void addLog(String category, String data, String code, String colorHex) {
        System.out.println(ConsoleColors.GREEN_BOLD+"["+new Timestamp(System.currentTimeMillis())+"] "+ConsoleColors.RESET+ConsoleColors.GREEN_BOLD+category+ConsoleColors.RESET+": "+ConsoleColors.GREEN_BOLD+data+ConsoleColors.RESET);
        executeSQL("INSERT INTO app_log (host_id, log_timestamp, log_category, log_data, log_code, log_color_hex) VALUES (0, '" + new Timestamp(System.currentTimeMillis()) + "', '" + category + "', '" + data + "', '" + code + "', '" + colorHex + "');");
    }

    /**
     * Method to add a log to the database for a specific host
     * @param hostId
     * @param category
     * @param data
     * @param code
     * @param colorHex
     */
    public void addHostLog(String hostId, String category, String data, String code, String colorHex) {
        System.out.println(ConsoleColors.BLUE_BOLD+"["+new Timestamp(System.currentTimeMillis())+"] "+ConsoleColors.RESET+ConsoleColors.BLUE_BOLD+category+ConsoleColors.RESET+": "+ConsoleColors.BLUE_BOLD+data+ConsoleColors.RESET);
        executeSQL("INSERT INTO app_log (host_id, log_timestamp, log_category, log_data, log_code, log_color_hex) VALUES (" + hostId + ", '" + new Timestamp(System.currentTimeMillis()) + "', '" + category + "', '" + data + "', '" + code + "', '" + colorHex + "');");
    }
}