/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.database_engine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.jakubwawak.entity.Host;
import com.jakubwawak.entity.PingData;
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

            // Enable WAL mode and set pragmas for better concurrency
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA busy_timeout=30000"); // 30 second timeout
                stmt.execute("PRAGMA synchronous=NORMAL"); // Faster writes with reasonable safety
            }

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
     * @param sql SQL query to execute
     * @return number of rows affected
     */
    public int executeSQL(String sql) {
        final int MAX_RETRIES = 5;
        final int INITIAL_WAIT_MS = 100;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                return stmt.getUpdateCount();
            } catch (SQLException e) {
                if (e.getMessage().contains("database is locked") || e.getMessage().contains("busy")) {
                    retryCount++;
                    if (retryCount == MAX_RETRIES) {
                        System.out.println(ConsoleColors.RED_BOLD + "Error: Database locked after " + MAX_RETRIES
                                + " retries: " + e.getMessage() + ConsoleColors.RESET);
                        break;
                    }

                    // Exponential backoff: wait longer after each retry
                    long waitTime = INITIAL_WAIT_MS * (long) Math.pow(2, retryCount - 1);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.out.println(ConsoleColors.RED_BOLD + "Error: Interrupted while waiting to retry: "
                                + ie.getMessage() + ConsoleColors.RESET);
                        break;
                    }
                    continue;
                }
                // If it's not a locking error, log and break immediately
                System.out.println(
                        ConsoleColors.RED_BOLD + "Error executing SQL: " + e.getMessage() + ConsoleColors.RESET);
                break;
            }
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
    private void createHostEntry(String hostName, String hostIp, String hostCategory, String hostDescription,
            int host_job_time, String host_status) {
        executeSQL(
                "INSERT INTO host_data (host_name, host_ip, host_category, host_description,host_status, host_job_time) VALUES ('"
                        + hostName + "', '" + hostIp + "', '" + hostCategory + "', '" + hostDescription + "', '"
                        + host_status + "', " + host_job_time + ");");
    }

    /**
     * Method to create the database
     */
    public void createDatabase() {
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
                    "host_description TEXT, " +
                    "host_status VARCHAR(20), " +
                    "host_job_time INTEGER);");

            createHostEntry("localhost", "127.0.0.1", "local", "Localhost", 30000, "active");
            addLog("info", "Localhost added", "info", ConsoleColors.GREEN_BOLD);
            createHostEntry("google.com", "8.8.8.8", "public", "Google DNS", 30000, "active");
            addLog("info", "Google DNS added", "info", ConsoleColors.GREEN_BOLD);
            createHostEntry("cloudflare.com", "1.1.1.1", "public", "Cloudflare DNS", 30000, "active");
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
                    "packet_tracert_data TEXT, " +
                    "packet_raw_ping TEXT);");
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
        System.out.println(ConsoleColors.GREEN_BOLD + "[" + new Timestamp(System.currentTimeMillis()) + "] "
                + ConsoleColors.RESET + ConsoleColors.GREEN_BOLD + category + ConsoleColors.RESET + ": "
                + ConsoleColors.GREEN_BOLD + data + ConsoleColors.RESET);
        executeSQL(
                "INSERT INTO app_log (host_id, log_timestamp, log_category, log_data, log_code, log_color_hex) VALUES (0, '"
                        + new Timestamp(System.currentTimeMillis()) + "', '" + category + "', '" + data + "', '" + code
                        + "', '" + colorHex + "');");
    }

    /**
     * Method to add a log to the database for a specific host
     * 
     * @param hostId
     * @param category
     * @param data
     * @param code
     * @param colorHex
     */
    public void addHostLog(int hostId, String category, String data, String code, String colorHex) {
        System.out.println(ConsoleColors.BLUE_BOLD + "[" + new Timestamp(System.currentTimeMillis()) + "] "
                + ConsoleColors.RESET + ConsoleColors.BLUE_BOLD + category + ConsoleColors.RESET + ": "
                + ConsoleColors.BLUE_BOLD + data + ConsoleColors.RESET);
        executeSQL(
                "INSERT INTO app_log (host_id, log_timestamp, log_category, log_data, log_code, log_color_hex) VALUES ("
                        + hostId + ", '" + new Timestamp(System.currentTimeMillis()) + "', '" + category + "', '" + data
                        + "', '" + code + "', '" + colorHex + "');");
    }

    /**
     * Method to add ping data to the database
     * 
     * @param pingData
     */
    public void addPingData(PingData pingData) {
        String sql = "INSERT INTO ping_history (host_id, ping_timestamp, packet_status_code, packet_status_color_hex, packet_transmitted, packet_received, packet_hop_time1, packet_hop_time2, packet_hop_time3, packet_hop_time4, packet_hop_time5, packet_hop_time6, packet_hop_time7, packet_hop_time8, packet_round_trip_time_min, packet_round_trip_time_max, packet_round_trip_time_avg, packet_dig_data, packet_tracert_data, packet_raw_ping) VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, pingData.hostId);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, pingData.packetStatusCode);
            pstmt.setString(4, pingData.packetStatusColorHex);
            pstmt.setInt(5, pingData.packetTransmitted);
            pstmt.setInt(6, pingData.packetReceived);
            pstmt.setDouble(7, pingData.packetHopTime1);
            pstmt.setDouble(8, pingData.packetHopTime2);
            pstmt.setDouble(9, pingData.packetHopTime3);
            pstmt.setDouble(10, pingData.packetHopTime4);
            pstmt.setDouble(11, pingData.packetHopTime5);
            pstmt.setDouble(12, pingData.packetHopTime6);
            pstmt.setDouble(13, pingData.packetHopTime7);
            pstmt.setDouble(14, pingData.packetHopTime8);
            pstmt.setDouble(15, pingData.packetRoundTripTimeMin);
            pstmt.setDouble(16, pingData.packetRoundTripTimeMax);
            pstmt.setDouble(17, pingData.packetRoundTripTimeAvg);
            pstmt.setString(18, pingData.packetDigData);
            pstmt.setString(19, pingData.packetTracertData);
            pstmt.setString(20, pingData.packetRawPing);
            pstmt.executeUpdate();

            addLog("info", "Ping data added to database", "info", ConsoleColors.GREEN_BOLD);

        } catch (SQLException e) {
            System.err.println("Error adding ping data: " + e.getMessage());
            addLog("error", "Error adding ping data: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
    }

    /**
     * Method to get all hosts from the database
     * 
     * @return ArrayList<Host>
     */
    public ArrayList<Host> getHosts() {
        ArrayList<Host> hosts = new ArrayList<>();
        String sql = "SELECT * FROM host_data;";
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                hosts.add(new Host(rs));
            }
            addLog("info", "Hosts fetched from database", "info", ConsoleColors.GREEN_BOLD);
        } catch (SQLException e) {
            addLog("error", "Error getting hosts: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
        return hosts;
    }

    /**
     * Method to close the database connection
     */
    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}