/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import com.jakubwawak.database_engine.DatabaseEngine;
import com.jakubwawak.ping_engine.PingEngine;

/**
 * Service for generating network statistics
 */
public class Pynk {

    public static final String VERSION = "1.0.0";
    public static final String BUILD = "pynk15042025REV01";

    public static DatabaseEngine databaseEngine;

    /**
     * Main application method
     * @param args
     */
    public static void main(String[] args) {
        showHeader();
        initDatabase();
        PingEngine pingEngine = new PingEngine();
        System.out.println(pingEngine.pingHost("8.8.8.8", 8));
        System.out.println(pingEngine.pingHost("192.168.1.123", 8));
    }

    /**
     * Initialize the database
     */
    static void initDatabase() {
        databaseEngine = new DatabaseEngine("./pynk.db");
    }

    /**
     * Print help message
     */
    static void showHeader() {
        System.out.println("Pynk - Service for generating network statistics");
        System.out.println("Version: " + VERSION);
        System.out.println("Build: " + BUILD);
    }
}
