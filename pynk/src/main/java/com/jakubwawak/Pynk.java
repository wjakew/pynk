/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.jakubwawak.database_engine.DatabaseEngine;
import com.jakubwawak.database_engine.DocumentDatabaseEngine;
import com.jakubwawak.entity.Host;
import com.jakubwawak.maintanance.ConsoleColors;
import com.jakubwawak.maintanance.Properties;
import com.jakubwawak.ping_engine.TraceRouteEngine;

/**
 * Service for generating network statistics
 */
public class Pynk {

    public static final String VERSION = "1.2.0";
    public static final String BUILD = "pynk12052025REV01";
    public static final boolean debug = false;

    public static DatabaseEngine databaseEngine;

    public static DocumentDatabaseEngine documentDatabaseEngine;

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
                    databaseEngine.addLog("host-refresh",
                            "Host data refreshed from database (size: " + activeHosts.size() + ")", "info", "#0000FF");
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
                                    "Host " + host.getHostName() + " is no longer active, stopping job thread", "info",
                                    "#0000FF");
                            break;
                        }

                        databaseEngine.addHostLog(currentHost.getHostId(), "thread-job",
                                "Starting job " + jobNumber + " for host " + currentHost.getHostName(), "info",
                                "#0000FF");
                        Job job = new Job(currentHost);
                        job.run();
                        databaseEngine.addHostLog(currentHost.getHostId(), "thread-job",
                                "Job " + jobNumber + " for host " + currentHost.getHostName() + " completed", "info",
                                "#0000FF");
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
     * Thread class for managing job threads for active hosts in MongoDB
     */
    private static class MongoHostManagerThread implements Runnable {
        private final ConcurrentHashMap<org.bson.types.ObjectId, Thread> jobThreads = new ConcurrentHashMap<>();
        private volatile boolean running = true;
        private final int refreshInterval = 5000; // 5 seconds
        private final int reconnectInterval = 21600000; // 6 hours in milliseconds

        @Override
        public void run() {
            // Start a separate thread for reconnecting to the database
            new Thread(this::reconnectDatabase).start();

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Get updated host list from MongoDB
                    ArrayList<Host> currentHosts = documentDatabaseEngine.getHosts();

                    documentDatabaseEngine.addLog("thread-manager",
                            "Processing " + currentHosts.size() + " hosts", "info", "#0000FF");

                    // Update job threads based on host status
                    for (Host host : currentHosts) {
                        documentDatabaseEngine.addLog("thread-manager",
                                "Checking host: " + host.getHostName() + " (ID: " + host.getHostIdMongo() + ") Status: "
                                        + host.getHostStatus(),
                                "info", "#0000FF");

                        if (host.getHostStatus().equals("active")) {
                            if (!jobThreads.containsKey(host.getHostIdMongo())) {
                                // Create new job thread for newly active host
                                Runnable jobRunnable = createMongoJobRunnable(host);
                                Thread jobThread = new Thread(jobRunnable);
                                jobThread.start();
                                jobThreads.put(host.getHostIdMongo(), jobThread);
                                documentDatabaseEngine.addLog("thread-manager",
                                        "Created new job thread for host " + host.getHostName(), "info", "#0000FF");
                            }
                        } else {
                            // Stop and remove thread for inactive host
                            Thread existingThread = jobThreads.remove(host.getHostIdMongo());
                            if (existingThread != null) {
                                existingThread.interrupt();
                                documentDatabaseEngine.addLog("thread-manager",
                                        "Stopped job thread for inactive host " + host.getHostName() + " (ID: "
                                                + host.getHostIdMongo() + ")",
                                        "info", "#0000FF");
                            }
                        }
                    }

                    // Remove threads for deleted hosts
                    jobThreads.entrySet().removeIf(entry -> {
                        org.bson.types.ObjectId hostId = entry.getKey();
                        boolean hostExists = currentHosts.stream()
                                .anyMatch(h -> h.hostIdMongo.equals(hostId));
                        if (!hostExists) {
                            entry.getValue().interrupt();
                            documentDatabaseEngine.addLog("thread-manager",
                                    "Removed job thread for deleted host (ID: " + hostId + ")", "info", "#0000FF");
                            return true;
                        }
                        return false;
                    });

                    Thread.sleep(refreshInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    documentDatabaseEngine.addLog("error", "Error in MongoDB host manager: " + e.getMessage(), "error",
                            "#FF0000");
                    try {
                        Thread.sleep(refreshInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        /**
         * Reconnect to the database
         */
        private void reconnectDatabase() {
            while (running) {
                try {
                    Thread.sleep(reconnectInterval);
                    documentDatabaseEngine.connect(); // Attempt to reconnect
                    documentDatabaseEngine.addLog("reconnect", "Reconnected to MongoDB", "info", "#00FF00");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    documentDatabaseEngine.addLog("error", "Error reconnecting to MongoDB: " + e.getMessage(), "error",
                            "#FF0000");
                }
            }
        }

        /**
         * Create a job runnable for MongoDB
         * 
         * @param host
         * @return Runnable
         */
        private Runnable createMongoJobRunnable(Host host) {
            return new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            // Get the latest host data from MongoDB
                            Host currentHost = documentDatabaseEngine.getHost(host.hostIdMongo);

                            // Skip if host is no longer active
                            if (currentHost == null || !currentHost.getHostStatus().equals("active")) {
                                documentDatabaseEngine.addLog("thread-job",
                                        "Host " + host.getHostName() + " is no longer active, stopping job thread",
                                        "info", "#0000FF");
                                break;
                            }

                            DocumentJob documentJob = new DocumentJob(currentHost);
                            documentJob.run();

                            Thread.sleep(currentHost.getHostJobTime());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } catch (Exception e) {
                            documentDatabaseEngine.addLog("error",
                                    "Error in job thread for host " + host.getHostName() + ": " + e.getMessage(),
                                    "error", "#FF0000");
                            try {
                                Thread.sleep(host.getHostJobTime());
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                            }
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
     * Thread class for periodic traceroute execution
     */
    private static class TraceRouteThread implements Runnable {
        private final String targetHost;
        private final int intervalMillis;
        private volatile boolean running = true;
        private final TraceRouteEngine traceRouteEngine;
        private final String databaseType;

        public TraceRouteThread(String targetHost, int intervalMinutes, String databaseType) {
            this.targetHost = targetHost;
            this.intervalMillis = intervalMinutes * 60 * 1000; // Convert minutes to milliseconds
            this.traceRouteEngine = new TraceRouteEngine();
            this.databaseType = databaseType;
        }

        @Override
        public void run() {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Execute traceroute
                    logMessage("Executing traceroute for host (" + targetHost + ")", "INFO", ConsoleColors.CYAN_BOLD_BRIGHT);
                    traceRouteEngine.executeTraceroute(targetHost);
                    logMessage("Traceroute for host (" + targetHost + ") completed", "INFO", ConsoleColors.CYAN_BOLD_BRIGHT);
                    // Sleep for the specified interval
                    Thread.sleep(intervalMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logMessage("Error in trace route thread: " + e.getMessage(), "error", "#FF0000");
                    try {
                        Thread.sleep(intervalMillis);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        private void logMessage(String message, String level, String color) {
            if (databaseType.equals("mongodb")) {
                documentDatabaseEngine.addLog("TRACE-ROUTE-THREAD", message, level, color);
            } else if (databaseType.equals("sqlite") && databaseEngine != null) {
                databaseEngine.addLog("TRACE-ROUTE-THREAD", message, level, color);
            }
        }

        public void stop() {
            running = false;
        }
    }

    /**
     * Main application method
     * 
     * @param args
     */
    public static void main(String[] args) {
        showHeader();
        properties = new Properties("./pynk.properties");
        if (properties.fileExists) {
            // Load properties
            properties.parsePropertiesFile();
            if (properties.getValue("databaseType").equals("sqlite")) {
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
            } else if (properties.getValue("databaseType").equals("mongodb")) {
                documentDatabaseEngine = new DocumentDatabaseEngine();
                documentDatabaseEngine.setDatabase_url(properties.getValue("databaseUrl"));
                documentDatabaseEngine.connect();

                if (!documentDatabaseEngine.connected) {
                    System.out.println("Failed to connect to MongoDB, please check the properties file");
                    System.exit(0);
                }

                if (debug) {
                    PynkTest pynkTest = new PynkTest();
                    pynkTest.run();
                } else {
                    documentDatabaseEngine.checkAndInitializeHostsCollection();

                    // Start the MongoDB host manager thread
                    System.out.println(ConsoleColors.RED_BOLD_BRIGHT+"Starting MongoDB host manager thread"+ConsoleColors.RESET);
                    MongoHostManagerThread mongoHostManagerThread = new MongoHostManagerThread();
                    Thread mongoManagerThread = new Thread(mongoHostManagerThread);
                    mongoManagerThread.setDaemon(true);
                    mongoManagerThread.start();

                    // Start the trace route thread
                    System.out.println(ConsoleColors.RED_BOLD_BRIGHT+"Starting trace route thread"+ConsoleColors.RESET);
                    TraceRouteThread traceRouteThread = new TraceRouteThread("8.8.8.8", 5, "mongodb"); // Execute every 5 minutes
                    Thread traceThread = new Thread(traceRouteThread);
                    traceThread.setDaemon(true);
                    traceThread.start();

                    // Keep the main thread alive
                    while (true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            } else {
                System.out.println("Invalid database type, please check the properties file");
                System.exit(0);
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
        System.out.println(ConsoleColors.PURPLE_BOLD_BRIGHT + "Pynk - Service for generating network statistics"
                + ConsoleColors.RESET);
        System.out.println("Version: " + VERSION);
        System.out.println("Build: " + BUILD);
    }
}
