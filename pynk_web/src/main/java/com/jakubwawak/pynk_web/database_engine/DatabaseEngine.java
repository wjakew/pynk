/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.database_engine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.jakubwawak.pynk_web.entity.Host;
import com.jakubwawak.pynk_web.entity.PingData;
import com.jakubwawak.pynk_web.maintanance.ConsoleColors;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Database engine for the application using MongoDB
 */
public class DatabaseEngine {

    public boolean connected = false;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private String databasePath;
    private final ReentrantLock connectionLock = new ReentrantLock();
    private static final int MAX_RETRIES = 5;
    private static final int INITIAL_WAIT_MS = 100;

    public static final String HOSTS_COLLECTION = "hosts";
    public static final String PING_HISTORY_COLLECTION = "ping_data";
    public static final String APP_LOG_COLLECTION = "logs";

    public static Document DEFAULT_CONFIGURATION;

    /**
     * Constructor to initialize the database path and connect
     * 
     * @param databasePath MongoDB connection string
     */
    public DatabaseEngine(String databasePath) {
        this.databasePath = databasePath;
        connect();
    }

    /**
     * Method to get the default configuration
     * 
     * @return Document
     */
    private Document getDefaultConfiguration(){
        Document doc = new Document();
        doc.append("allow_ping_history_deletion", false);
        return doc;
    }

    /**
     * Method to connect to MongoDB
     */
    public void connect() {
        try {
            connectionLock.lock();
            try {
                if (mongoClient != null) {
                    mongoClient.close();
                }

                mongoClient = MongoClients.create(databasePath);
                database = mongoClient.getDatabase("db_pynk");
                connected = true;
                addLog("info", "Connected to MongoDB", "info", ConsoleColors.GREEN_BOLD);
                createConfigurationEntry();
            } finally {
                connectionLock.unlock();
            }
        } catch (Exception e) {
            connected = false;
            e.printStackTrace();
        }
    }

    /**
     * Method to create a configuration entry in the database
     */
    public void createConfigurationEntry(){
        Document doc = getDefaultConfiguration();
        try {
            MongoCollection<Document> configCollection = getCollection("configuration");
            if (configCollection.countDocuments() > 0) {
                addLog("info", "Configuration entry already exists - loading it", "info", ConsoleColors.YELLOW_BOLD);
                DEFAULT_CONFIGURATION = configCollection.find().first();
            } else {
                addLog("info", "Configuration entry created successfully", "info", ConsoleColors.GREEN_BOLD);
                configCollection.insertOne(doc);
            }
        } catch (Exception e) {
            addLog("error", "Error creating/updating configuration entry: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
    }

    /**
     * Method to update the configuration entry in the database
     * 
     * @param doc
     */
    public void updateConfigurationEntry(Document doc){
        try {
            getCollection("configuration").updateOne(new Document(), new Document("$set", doc));
            addLog("info", "Configuration entry updated successfully", "info", ConsoleColors.GREEN_BOLD);
        } catch (Exception e) {
            addLog("error", "Error updating configuration entry: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
    }

    /**
     * Method to set the allow ping history deletion in the configuration entry
     * 
     * @param allowPingHistoryDeletion
     */
    public void setConfigurationAllowPingHistoryDeletion(boolean allowPingHistoryDeletion){
        DEFAULT_CONFIGURATION.put("allow_ping_history_deletion", allowPingHistoryDeletion);
        updateConfigurationEntry(DEFAULT_CONFIGURATION);
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
     * Method to create a host entry in the database
     */
    private void createHostEntry(String hostName, String hostIp, String hostCategory, String hostDescription,
            int host_job_time, String host_status) {
        Document doc = new Document()
                .append("hostName", hostName)
                .append("hostIp", hostIp)
                .append("hostCategory", hostCategory)
                .append("hostDescription", hostDescription)
                .append("hostStatus", host_status)
                .append("hostJobTime", host_job_time);

        getCollection(HOSTS_COLLECTION).insertOne(doc);
    }

    /**
     * Method to add a host to the database
     * 
     * @param host
     * @return 1 if successful, 0 if failed
     */
    public int addHost(Host host) {
        try {
            InsertOneResult result = getCollection(HOSTS_COLLECTION).insertOne(host.toDocument());
            return result.wasAcknowledged() ? 1 : 0;
        } catch (Exception e) {
            addLog("error", "Error adding host: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
            return 0;
        }
    }

    /**
     * Method to update a host in the database
     * 
     * @param host
     * @return 1 if successful, 0 if failed
     */
    public int updateHost(Host host) {
        try {
            UpdateResult result = getCollection(HOSTS_COLLECTION)
                    .updateOne(
                            Filters.eq("_id", host.getHostIdMongo()),
                            Updates.combine(
                                    Updates.set("hostName", host.getHostName()),
                                    Updates.set("hostIp", host.getHostIp()),
                                    Updates.set("hostCategory", host.getHostCategory()),
                                    Updates.set("hostDescription", host.getHostDescription()),
                                    Updates.set("hostStatus", host.getHostStatus()),
                                    Updates.set("hostJobTime", host.getHostJobTime())));
            return result.getModifiedCount() > 0 ? 1 : 0;
        } catch (Exception e) {
            addLog("error", "Error updating host: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
            return 0;
        }
    }

    /**
     * Method to delete a host from the database
     * 
     * @param hostId
     * @return 1 if successful, 0 if failed
     */
    public int deleteHost(ObjectId hostId) {
        try {
            Host host = getHostById(hostId);
            if (host != null && host.getHostIdMongo() != null) {
                DeleteResult result = getCollection(HOSTS_COLLECTION)
                        .deleteOne(Filters.eq("_id", host.getHostIdMongo()));
                return result.getDeletedCount() > 0 ? 1 : 0;
            }
            return 0;
        } catch (Exception e) {
            addLog("error", "Error deleting host: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
            return 0;
        }
    }

    /**
     * Method to delete the ping history for a host
     * 
     * @param hostId
     * @return 1 if successful, 0 if failed
     */
    public int deleteHostPingHistory(ObjectId hostId) {
        try {
            Host host = getHostById(hostId);
            if (host != null && host.getHostIdMongo() != null) {
                DeleteResult result = getCollection(PING_HISTORY_COLLECTION)
                        .deleteMany(Filters.eq("host_id", host.getHostIdMongo()));
                return result.getDeletedCount() > 0 ? 1 : 0;
            }
            return 0;
        } catch (Exception e) {
            addLog("error", "Error deleting ping history: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
            return 0;
        }
    }

    /**
     * Method to get all unique host statuses from the database
     * 
     * @return ArrayList<String>
     */
    public ArrayList<String> getAllUniqueHostStatuses() {
        ArrayList<String> statuses = new ArrayList<>();
        try {
            getCollection(HOSTS_COLLECTION)
                    .distinct("hostStatus", String.class)
                    .into(statuses);
        } catch (Exception e) {
            addLog("error", "Error getting unique host statuses: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
        return statuses;
    }

    /**
     * Method to get all unique host categories from the database
     * 
     * @return ArrayList<String>
     */
    public ArrayList<String> getAllUniqueHostCategories() {
        ArrayList<String> categories = new ArrayList<>();
        try {
            getCollection(HOSTS_COLLECTION)
                    .distinct("hostCategory", String.class)
                    .into(categories);
        } catch (Exception e) {
            addLog("error", "Error getting unique host categories: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
        return categories;
    }

    /**
     * Method to get app log data between dates
     * 
     * @param startDate
     * @param endDate
     * @return ArrayList<Document>
     */
    public ArrayList<Document> getAppLogDataBetweenDates(Timestamp startDate, Timestamp endDate) {
        ArrayList<Document> logs = new ArrayList<>();
        try {
            getCollection(APP_LOG_COLLECTION).find(Filters.and(Filters.gte("log_timestamp", startDate), Filters.lte("log_timestamp", endDate))).into(logs);
        } catch (Exception e) {
            addLog("error", "Error getting app log data between dates: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
        return logs;
    }

    /**
     * Method to add a log to the database
     */
    public void addLog(String category, String data, String code, String colorHex) {
        System.out.println(ConsoleColors.GREEN_BOLD + "[" + new Timestamp(System.currentTimeMillis()) + "] "
                + ConsoleColors.RESET + ConsoleColors.GREEN_BOLD + category + ConsoleColors.RESET + ": "
                + ConsoleColors.GREEN_BOLD + data + ConsoleColors.RESET);

        Document doc = new Document()
                .append("host_id", new ObjectId()) // Default ObjectId for system logs
                .append("log_timestamp", new Date())
                .append("log_category", category)
                .append("log_data", data)
                .append("log_code", code)
                .append("log_color_hex", colorHex);

        getCollection(APP_LOG_COLLECTION).insertOne(doc);
    }

    /**
     * Method to add a log to the database for a specific host
     */
    public void addHostLog(ObjectId hostId, String category, String data, String code, String colorHex) {
        System.out.println(ConsoleColors.BLUE_BOLD + "[" + new Timestamp(System.currentTimeMillis()) + "] "
                + ConsoleColors.RESET + ConsoleColors.BLUE_BOLD + category + ConsoleColors.RESET + ": "
                + ConsoleColors.BLUE_BOLD + data + ConsoleColors.RESET);

        Host host = getHostById(hostId);
        if (host != null && host.getHostIdMongo() != null) {
            Document doc = new Document()
                    .append("host_id", host.getHostIdMongo())
                    .append("log_timestamp", new Date())
                    .append("log_category", category)
                    .append("log_data", data)
                    .append("log_code", code)
                    .append("log_color_hex", colorHex);

            getCollection(APP_LOG_COLLECTION).insertOne(doc);
        }
    }

    /**
     * Method to add ping data to the database
     * 
     * @param pingData
     */
    public void addPingData(PingData pingData) {
        try {
            getCollection(PING_HISTORY_COLLECTION).insertOne(pingData.toDocument());
            addLog("info", "Ping data added to database", "info", ConsoleColors.GREEN_BOLD);
        } catch (Exception e) {
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
        try {
            getCollection(HOSTS_COLLECTION)
                    .find()
                    .map(doc -> new Host(doc))
                    .into(hosts);
            addLog("info", "Hosts fetched from database", "info", ConsoleColors.GREEN_BOLD);
        } catch (Exception e) {
            addLog("error", "Error getting hosts: " + e.getMessage(), "error", ConsoleColors.RED_BOLD);
        }
        return hosts;
    }

    /**
     * Method to get a host by id
     * 
     * @param hostId
     * @return Host
     */
    public Host getHostById(ObjectId hostId) {
        try {
            Document doc = getCollection(HOSTS_COLLECTION)
                    .find(Filters.eq("_id", hostId))
                    .first();
            if (doc != null) {
                return new Host(doc);
            }
            addLog("warning", "No host found with ID: " + hostId, "warning", "#FFA500");
        } catch (Exception e) {
            addLog("error", "Error getting host by id " + hostId + ": " + e.getMessage(), "error", "#FF0000");
        }
        return null;
    }

    /**
     * Helper method to get a collection
     * 
     * @param collectionName
     * @return MongoCollection<Document>
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    /**
     * Method to close the database connection
     */
    public void closeConnection() {
        connectionLock.lock();
        try {
            if (mongoClient != null) {
                mongoClient.close();
                connected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connectionLock.unlock();
        }
    }
}