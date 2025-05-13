/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.database_engine;

import com.jakubwawak.entity.Host;
import com.jakubwawak.entity.PingData;
import com.jakubwawak.entity.TraceSinglePath;
import com.jakubwawak.maintanance.ConsoleColors;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.mongodb.client.model.Filters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * DocumentDatabaseEngine class for managing database connections and operations
 */
public class DocumentDatabaseEngine {

    public String database_url;
    public boolean connected;
    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    ArrayList<String> error_collection;

    /**
     * Constructor
     */
    public DocumentDatabaseEngine() {
        this.database_url = "";
        connected = false;
        error_collection = new ArrayList<>();
    }

    /**
     * Function for setting database URL
     * 
     * @param database_url
     */
    public void setDatabase_url(String database_url) {
        this.database_url = database_url;
    }

    /**
     * Function for connecting to database
     * 
     * @return boolean
     */
    public void connect() {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        // Create a CodecRegistry that includes POJO support
        CodecRegistry pojoCodecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            fromProviders(PojoCodecProvider.builder().automatic(true).build())
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(database_url))
                .serverApi(serverApi)
                .codecRegistry(pojoCodecRegistry)  // Add the POJO codec registry
                .build();
        try {
            log("DB-CONNECTION", "Connecting to database...");
            mongoClient = MongoClients.create(settings);
            MongoDatabase database = mongoClient.getDatabase("admin");
            // Send a ping to confirm a successful connection
            Bson command = new BsonDocument("ping", new BsonInt64(1));
            Document commandResult = database.runCommand(command);
            connected = true;
            mongoDatabase = mongoClient.getDatabase("db_pynk");
            log("DB-CONNECTION", "Connected succesffully with database - running application");
        } catch (MongoException ex) {
            // catch error
            log("DB-CONNECTION-ERROR", "Failed to connect to database (" + ex.toString() + ")");
            connected = false;
        }
    }

    /**
     * Function for checking and initializing hosts collection
     */
    public void checkAndInitializeHostsCollection() {
        try {
            // Check if collection exists
            boolean collectionExists = false;
            for (String collectionName : mongoDatabase.listCollectionNames()) {
                if (collectionName.equals("hosts")) {
                    collectionExists = true;
                    break;
                }
            }

            if (!collectionExists) {
                // Create collection and insert default hosts
                mongoDatabase.createCollection("hosts");
                MongoCollection<Document> hostsCollection = mongoDatabase.getCollection("hosts");

                // Create documents for default hosts
                Document[] defaultHosts = {
                        new Document("hostIp", "1.1.1.1")
                                .append("hostName", "Cloudflare DNS")
                                .append("hostStatus", "active")
                                .append("hostJobTime", 30000)
                                .append("hostCategory", "public")
                                .append("hostDescription", "Cloudflare DNS"),
                        new Document("hostIp", "8.8.8.8")
                                .append("hostName", "Google DNS")
                                .append("hostStatus", "active")
                                .append("hostJobTime", 30000)
                                .append("hostCategory", "public")
                                .append("hostDescription", "Google DNS"),
                        new Document("hostIp", "127.0.0.1")
                                .append("hostName", "Localhost")
                                .append("hostStatus", "active")
                                .append("hostJobTime", 30000)
                                .append("hostCategory", "local")
                                .append("hostDescription", "Localhost"),
                        new Document("hostIp", "8.8.4.4")
                                .append("hostName", "Google DNS Secondary")
                                .append("hostStatus", "active")
                                .append("hostJobTime", 30000)
                                .append("hostCategory", "public")
                                .append("hostDescription", "Google DNS Secondary")
                };
                // Insert all default hosts
                hostsCollection.insertMany(Arrays.asList(defaultHosts));
                log("DB-INIT", "Created hosts collection with default entries");
            }
        } catch (MongoException ex) {
            log("DB-INIT-ERROR", "Failed to initialize hosts collection (" + ex.toString() + ")");
        }
    }

    /**
     * Function for getting collection from database
     * 
     * @param collection_name
     * @return MongoCollection<Document>
     */
    public MongoCollection<Document> getCollection(String collection_name) {
        return mongoDatabase.getCollection(collection_name);
    }

    /**
     * Function for inserting document to collection
     * 
     * @param collectionName
     * @param document
     * @return int
     */
    public int insert(String collectionName, Document document) {
        try {
            InsertOneResult result = mongoDatabase.getCollection(collectionName).insertOne(document);
            if (result.getInsertedId() != null) {
                return 1;
            } else {
                log("DB-INSERT-ERROR", "Failed to insert document to collection, document: " + document.toString());
                return 0;
            }
        } catch (MongoException ex) {
            log("DB-INSERT-ERROR", "Failed to insert document to collection (" + ex.toString() + ")");
            return -1;
        }
    }

    /**
     * Function for updating document in collection
     * 
     * @param collectionName
     * @param id
     * @param document
     * @return int
     *         1 - success
     *         0 - failed
     *         -1 - error
     */
    public int update(String collectionName, ObjectId id, Document document) {
        try {
            UpdateResult result = getCollection(collectionName).updateOne(Filters.eq("_id", id),
                    new Document("$set", document));
            if (result.getModifiedCount() == 1) {
                log("DB-UPDATE", "Updated document in collection (" + id + ")");
                return 1;
            } else {
                log("DB-UPDATE-ERROR", "Failed to update document in collection (" + id + ")");
                return 0;
            }
        } catch (Exception e) {
            log("DB-UPDATE-ERROR", e.getMessage());
            return -1;
        }
    }

    /**
     * Function for removing session
     * 
     * @param session_id
     * @return int
     *         1 - success
     *         0 - session not found
     */
    public int removeSession(String session_id) {
        DeleteResult result = getCollection("sessions").deleteOne(Filters.eq("session_id", session_id));
        if (result.getDeletedCount() == 1) {
            log("SESSION-REMOVE", "Session removed (" + session_id + ")");
            return 1;
        }
        return 0;
    }

    /**
     * Function to manage user sessions
     * Checks if a user has 3 sessions and removes the oldest if so.
     * 
     * @param userId the ID of the user
     */
    void manageUserSessions(ObjectId userId) {
        List<Document> sessions = getCollection("sessions").find(Filters.eq("user_id", userId))
                .sort(Sorts.ascending("created_at")).into(new ArrayList<>());
        if (sessions.size() >= 3) {
            // Remove the oldest session
            Document oldestSession = sessions.get(0);
            getCollection("sessions").deleteOne(Filters.eq("session_id", oldestSession.getString("session_id")));
            log("SESSION-MANAGEMENT", "Removed oldest session for user (" + userId + ")");
        } else {
            log("SESSION-MANAGEMENT",
                    "No sessions to remove for user (" + userId + "), amount of sessions: " + sessions.size());
        }
    }

    /**
     * Function for adding ping data
     * 
     * @param pingData
     */
    public void addPingData(PingData pingData) {
        Document pingDataDocument = pingData.toDocument();
        int result = insert("ping_data", pingDataDocument);
        if (result == 1) {
            log("DB-PING-DATA", "Added ping data for host (" + pingData.hostIdMongo + ") ping_timestamp: "
                    + pingData.pingTimestamp);
        } else {
            log("DB-PING-DATA-ERROR", "Failed to add ping data for host (" + pingData.hostIdMongo + ")");
        }
    }

    /**
     * Function for adding hour default ping data
     * 
     * @param pingData
     */
    public void addHourDefaultPingData(PingData pingData){
        Document hourDocument = new Document()
            .append("ping_timestamp", pingData.pingTimestamp)
            .append("ping_avg", pingData.packetRoundTripTimeAvg)
            .append("ping_min", pingData.packetRoundTripTimeMin)
            .append("ping_max", pingData.packetRoundTripTimeMax);
        int result = insert("hour_default_ping_data", hourDocument);
        if (result == 1) {
            log("DB-HOUR-DEFAULT-PING-DATA", "Added hour default ping data for host (" + pingData.hostIdMongo + ")");
        } else {
            log("DB-HOUR-DEFAULT-PING-DATA-ERROR", "Failed to add hour default ping data for host (" + pingData.hostIdMongo + ")");
        }
    }

    /**
     * Function for adding trace route data
     * 
     * @param traceRouteData
     */
    public void addTraceRouteData(ArrayList<TraceSinglePath> traceRouteData, String hostName) {
        try {
            // Get the collection with POJO codec support
            MongoCollection<Document> collection = mongoDatabase.getCollection("trace_route_data")
                .withCodecRegistry(mongoDatabase.getCodecRegistry());

            // Create the document with proper field access
            Document traceRouteDataDocument = new Document()
                .append("host_name", hostName)
                .append("timestamp", System.currentTimeMillis())
                .append("hops", traceRouteData.stream().map(hop -> new Document()
                    .append("name", hop.getName())
                    .append("ip", hop.getIp())
                    .append("max", hop.getMax())
                    .append("min", hop.getMin())
                    .append("avg", hop.getAvg())
                ).toList());
            
            int result = insert("trace_route_data", traceRouteDataDocument);
            if (result == 1) {
                log("DB-TRACE-ROUTE-DATA", "Added trace route data for host (" + hostName + ")");
            } else {
                log("DB-TRACE-ROUTE-DATA-ERROR", "Failed to add trace route data for host (" + hostName + ")");
            }
        } catch (Exception e) {
            log("DB-TRACE-ROUTE-DATA-ERROR", "Failed to add trace route data: " + e.getMessage());
        }
    }

    /**
     * Function for adding host
     * 
     * @param host
     */
    public void addHost(Host host) {
        Document hostDocument = host.toDocument();
        int result = insert("hosts", hostDocument);
        if (result == 1) {
            log("DB-HOST", "Added host (" + host.hostId + ")");
        } else {
            log("DB-HOST-ERROR", "Failed to add host (" + host.hostId + ")");
        }
    }

    /**
     * Function for adding host log
     * 
     * @param hostId
     * @param category
     * @param data
     * @param code
     * @param colorHex
     */
    public void addHostLog(int hostId, String category, String data, String code, String colorHex) {
        Document logEntry = new Document("host_id", hostId)
                .append("category", category)
                .append("data", data)
                .append("code", code)
                .append("colorHex", colorHex);
        int result = insert("host_logs", logEntry);
        if (result == 1) {
            System.out.println(ConsoleColors.BLUE_BOLD_BRIGHT + category + "["
                    + LocalDateTime.now(ZoneId.of("Europe/Warsaw")).toString() + ") - lvl: " + code + " - " + data
                    + ConsoleColors.RESET);
        } else {
            log("DB-HOST-LOG-ERROR", "Failed to add host log for host (" + hostId + ")");
        }
    }

    /**
     * Function for getting hosts
     * 
     * @return ArrayList<Host>
     */
    public ArrayList<Host> getHosts() {
        ArrayList<Host> hosts = new ArrayList<>();
        List<Document> documents = getCollection("hosts").find().into(new ArrayList<>());
        for (Document document : documents) {
            hosts.add(new Host(document));
        }
        log("DB-HOST", "Found " + hosts.size() + " hosts");
        return hosts;
    }

    /**
     * Function for getting a single host by ID
     * 
     * @param hostId
     * @return Host or null if not found
     */
    public Host getHost(ObjectId hostId) {
        Document document = getCollection("hosts").find(Filters.eq("_id", hostId)).first();
        if (document != null) {
            return new Host(document);
        }
        return null;
    }

    /**
     * Function for adding a log entry with color
     * 
     * @param category
     * @param message
     * @param level
     * @param colorHex
     */
    public void addLog(String category, String message, String level, String colorHex) {
        Document logEntry = new Document()
                .append("category", category)
                .append("message", message)
                .append("level", level)
                .append("color_hex", colorHex)
                .append("timestamp", LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        int result = insert("logs", logEntry);
        if (result == 1) {
            System.out.println(ConsoleColors.GREEN_BRIGHT + category + "["
                    + LocalDateTime.now(ZoneId.of("Europe/Warsaw")).toString() + ") - lvl: " + level + " - " + message
                    + ConsoleColors.RESET);
        } else {
            log("DB-LOG-ERROR", "Failed to add log entry: " + message);
        }
    }

    /**
     * Function for creating log entry
     * 
     * @param log_category
     * @param log_text
     */
    private void createLogEntry(String log_category, String log_text) {
        try {
            MongoCollection<Document> collection = mongoDatabase.getCollection("logs");
            Document logEntry = new Document("category", log_category)
                    .append("text", log_text)
                    .append("timestamp", LocalDateTime.now(ZoneId.of("Europe/Warsaw")));
            collection.insertOne(logEntry);
        } catch (Exception ex) {
            log("DB-LOG-ERROR", "Failed to create log entry (" + ex.toString() + ")");
        }
    }

    /**
     * Function for story log data
     * 
     * @param log_category
     * @param log_text
     */
    public void log(String log_category, String log_text) {
        error_collection
                .add(log_category + "(" + LocalDateTime.now(ZoneId.of("Europe/Warsaw")).toString() + ") - " + log_text);
        if (log_category.contains("FAILED") || log_category.contains("ERROR")) {
            System.out.println(ConsoleColors.RED_BRIGHT + log_category + "["
                    + LocalDateTime.now(ZoneId.of("Europe/Warsaw")).toString() + ") - " + log_text + "]"
                    + ConsoleColors.RESET);
        } else {
            System.out.println(ConsoleColors.GREEN_BRIGHT + log_category + "["
                    + LocalDateTime.now(ZoneId.of("Europe/Warsaw")).toString() + ") - " + log_text + "]"
                    + ConsoleColors.RESET);
        }
        if (connected) {
            createLogEntry(log_category, log_text);
        }
    }

}
