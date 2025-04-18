/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
    public static ConcurrentHashMap<Integer, Host> activeHosts = new ConcurrentHashMap<>();

    /**
     * Thread class for refreshing host data periodically
     */
    private static class HostRefreshThread implements Runnable {
        private final int refreshInterval;
        private volatile boolean running = true;

        public HostRefreshThread(int refreshIntervalSeconds) {
            this.refreshInterval = refreshIntervalSeconds * 1000; // Convert to milliseconds
        }

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    ArrayList<Host> updatedHosts = databaseEngine.getHosts();
                    // Update the shared hosts map
                    activeHosts.clear();
                    for (Host host : updatedHosts) {
                        if (host.getHostStatus().equals("active")) {
                            activeHosts.put(host.getHostId(), host);
                        }
                    }
                    databaseEngine.addLog("host-refresh", "Host data refreshed from database (size: " + activeHosts.size() + ")", "info", "#0000FF");
                    Thread.sleep(refreshInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    databaseEngine.addLog("error", "Error refreshing hosts: " + e.getMessage(), "error", "#FF0000");
                    try {
                        Thread.sleep(refreshInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        public void stop() {
            running = false;
        }
    }

    /**
     * Thread class for managing job threads for active hosts
     */
    private static class JobManagerThread implements Runnable {
        private final ConcurrentHashMap<Integer, Thread> jobThreads = new ConcurrentHashMap<>();
        private volatile boolean running = true;

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Check active hosts and create/remove job threads as needed
                    for (Host host : activeHosts.values()) {
                        if (!jobThreads.containsKey(host.getHostId())) {
                            // Create new job thread for newly active host
                            Runnable jobRunnable = createJobRunnable(host);
                            Thread jobThread = new Thread(jobRunnable);
                            jobThread.start();
                            jobThreads.put(host.getHostId(), jobThread);
                            databaseEngine.addHostLog(host.getHostId(), "thread-manager", 
                                "Created new job thread for host " + host.getHostName(), "info", "#0000FF");
                        }
                    }

                    // Remove threads for inactive hosts
                    jobThreads.entrySet().removeIf(entry -> {
                        int hostId = entry.getKey();
                        if (!activeHosts.containsKey(hostId)) {
                            Thread thread = entry.getValue();
                            thread.interrupt();
                            databaseEngine.addLog("thread-manager", 
                                "Removed job thread for inactive host (ID: " + hostId + ")", "info", "#0000FF");
                            return true;
                        }
                        return false;
                    });

                    Thread.sleep(5000); // Check every 5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    databaseEngine.addLog("error", "Error in job manager: " + e.getMessage(), "error", "#FF0000");
                }
            }
        }

        private Runnable createJobRunnable(Host host) {
            return new Runnable() {
                @Override
                public void run() {
                    int jobNumber = 0;
                    while (!Thread.currentThread().isInterrupted()) {
                        // Get the latest host data from the shared map
                        Host currentHost = activeHosts.get(host.getHostId());
                        
                        // Skip if host is no longer active or has been removed
                        if (currentHost == null || !currentHost.getHostStatus().equals("active")) {
                            databaseEngine.addLog("thread-job", 
                                "Host " + host.getHostName() + " is no longer active, stopping job thread", "info", "#0000FF");
                            break;
                        }

                        databaseEngine.addHostLog(currentHost.getHostId(), "thread-job", 
                            "Starting job " + jobNumber + " for host " + currentHost.getHostName(), "info", "#0000FF");
                        Job job = new Job(currentHost);
                        job.run();
                        databaseEngine.addHostLog(currentHost.getHostId(), "thread-job", 
                            "Job " + jobNumber + " for host " + currentHost.getHostName() + " completed", "info", "#0000FF");
                        jobNumber++;
                        try {
                            Thread.sleep(currentHost.getHostJobTime());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            };
        }

        public void stop() {
            running = false;
            // Interrupt all job threads
            for (Thread thread : jobThreads.values()) {
                thread.interrupt();
            }
        }
    }

    /**
     * Main application method
     * @param args
     */
    public static void main(String[] args) {
        showHeader();
        properties = new Properties("./pynk.properties");
        if (properties.fileExists) {
            // Load properties
            properties.parsePropertiesFile();
            initDatabase(properties.getValue("databasePath")); // Initialize database

            // Start the host refresh thread
            HostRefreshThread hostRefreshThread = new HostRefreshThread(30); // Refresh every 30 seconds
            Thread refreshThread = new Thread(hostRefreshThread);
            refreshThread.setDaemon(true);
            refreshThread.start();

            // Start the job manager thread
            JobManagerThread jobManagerThread = new JobManagerThread();
            Thread managerThread = new Thread(jobManagerThread);
            managerThread.setDaemon(true);
            managerThread.start();

            // Keep the main thread alive
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
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
