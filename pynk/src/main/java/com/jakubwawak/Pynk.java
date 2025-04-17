/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import java.util.ArrayList;

import com.jakubwawak.database_engine.DatabaseEngine;
import com.jakubwawak.entity.Host;
import com.jakubwawak.maintanance.Properties;

/**
 * Service for generating network statistics
 */
public class Pynk {

    public static final String VERSION = "1.0.0";
    public static final String BUILD = "pynk17042025REV01";

    public static DatabaseEngine databaseEngine;
    public static Properties properties;
    /**
     * Main application method
     * @param args
     */
    public static void main(String[] args) {
        showHeader();
        properties = new Properties("./pynk.properties");
        if( properties.fileExists ) {
            // Load properties
            properties.parsePropertiesFile();
            initDatabase(properties.getValue("databasePath")); // Initialize database

            // create jobs - create pipeline TODO

            ArrayList<Host> hosts = databaseEngine.getHosts();
            for (Host host : hosts) {
                if( host.getHostStatus().equals("active")) {
                    Runnable jobRunnable = new Runnable() {
                        @Override
                        public void run() {
                        int jobNumber = 0;
                        while (true) {
                            Pynk.databaseEngine.addHostLog(host.getHostId(), "thread-job", "Starting job " + jobNumber + " for host " + host.getHostName(), "info", "#0000FF");
                            Job job = new Job(host);
                            job.run();
                            Pynk.databaseEngine.addHostLog(host.getHostId(), "thread-job", "Job " + jobNumber + " for host " + host.getHostName() + " completed", "info", "#0000FF");
                            jobNumber++;
                            try {
                                Thread.sleep(host.getHostJobTime()); // time in miliseconds
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break; // Exit the loop if interrupted
                            }
                        }
                    }
                };
                    Thread jobThread = new Thread(jobRunnable);
                    jobThread.start();
                }
                else{
                    Pynk.databaseEngine.addHostLog(host.getHostId(), "thread-job", "Host " + host.getHostName() + " is not active, skipping", "info", "#0000FF");
                }
            }



        } else {
            // Create properties file
            System.out.println("Properties file not found, creating new one");
            properties.createPropertiesFile();
            System.out.println("Properties file created, please configure it and run the application again");
            System.exit(0);
        }
    }

    /**
     * Initialize the database
     */
    static void initDatabase(String databasePath) {
        databaseEngine = new DatabaseEngine(databasePath);
        databaseEngine.connect();
        databaseEngine.createDatabase();
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
