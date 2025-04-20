/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.database_engine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.jakubwawak.pynk_web.entity.Host;
import com.jakubwawak.pynk_web.entity.PingData;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Database data engine for MongoDB
 */
public class DatabaseDataEngine {

    DatabaseEngine databaseEngine;

    /**
     * Constructor
     * 
     * @param databaseEngine
     */
    public DatabaseDataEngine(DatabaseEngine databaseEngine) {
        this.databaseEngine = databaseEngine;
    }

    /**
     * Get ping data between dates
     * 
     * @param startDate
     * @param endDate
     * @return ping data between dates
     */
    public ArrayList<PingData> getPingDataBetweenDates(Timestamp startDate, Timestamp endDate) {
        ArrayList<PingData> pingData = new ArrayList<>();
        try {
            databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                    .find(Filters.and(
                            Filters.gt("ping_timestamp", new Date(startDate.getTime())),
                            Filters.lt("ping_timestamp", new Date(endDate.getTime()))))
                    .sort(new Document("ping_timestamp", -1)) // DESC order
                    .map(doc -> new PingData(doc))
                    .into(pingData);

            databaseEngine.addLog("DatabaseDataEngine", "Successfully got ping data between dates " + startDate
                    + " and " + endDate + " with " + pingData.size() + " rows", "INFO", "#00FF00");
            return pingData;
        } catch (Exception e) {
            databaseEngine.addLog("DatabaseDataEngine", "Error getting ping data between dates " + e.getMessage(),
                    "ERROR", "#FF0000");
            return null;
        }
    }

    /**
     * Get ping data between dates for a specific host
     * 
     * @param host
     * @param startDate
     * @param endDate
     * @return ping data between dates for a specific host
     */
    public ArrayList<PingData> getPingDataBetweenDates(Host host, Timestamp startDate, Timestamp endDate) {
        ArrayList<PingData> pingData = new ArrayList<>();
        try {
            databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                    .find(Filters.and(
                            Filters.eq("host_id", host.getHostIdMongo()),
                            Filters.gt("ping_timestamp", new Date(startDate.getTime())),
                            Filters.lt("ping_timestamp", new Date(endDate.getTime()))))
                    .sort(new Document("ping_timestamp", -1)) // DESC order
                    .map(doc -> new PingData(doc))
                    .into(pingData);

            databaseEngine.addLog("DatabaseDataEngine", "Successfully got ping data between dates " + startDate
                    + " and " + endDate + " with " + pingData.size() + " rows", "INFO", "#00FF00");
            return pingData;
        } catch (Exception e) {
            databaseEngine.addLog("DatabaseDataEngine", "Error getting ping data between dates " + e.getMessage(),
                    "ERROR", "#FF0000");
            return null;
        }
    }

    /**
     * Get number of successes from last 24 hours
     * 
     * @return number of successes from last 24 hours
     */
    public int getNumberOfSuccessesFrom24h() {
        try {
            Document result = databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                    .aggregate(java.util.Arrays.asList(
                            Aggregates.match(Filters.and(
                                    Filters.gt("ping_timestamp",
                                            new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                    Filters.lt("ping_timestamp", new Date(System.currentTimeMillis())))),
                            Aggregates.group(null, Accumulators.sum("packet_status_code", "Success"))))
                    .first();

            if (result != null) {
                return result.getInteger("successes");
            }
            return 0;
        } catch (Exception e) {
            databaseEngine.addLog("DatabaseDataEngine", "Error getting number of successes from last 24 hours ("
                    + e.getMessage() + ")", "ERROR", "#FF0000");
            return 0;
        }
    }

    /**
     * Get number of failures from last 24 hours
     * 
     * @return number of failures from last 24 hours
     */
    public int getNumberOfFailuresFrom24h() {
        try {
            Document result = databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                    .aggregate(java.util.Arrays.asList(
                            Aggregates.match(Filters.and(
                                    Filters.gt("ping_timestamp",
                                            new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                    Filters.lt("ping_timestamp", new Date(System.currentTimeMillis())),
                                    Filters.ne("packet_status_code", "Success"))),
                            Aggregates.group(null, Accumulators.sum("packet_status_code", 1))))
                    .first();

            if (result != null) {
                return result.getInteger("failures");
            }
            return 0;
        } catch (Exception e) {
            databaseEngine.addLog("DatabaseDataEngine", "Error getting number of failures from last 24 hours ("
                    + e.getMessage() + ")", "ERROR", "#FF0000");
            return 0;
        }
    }

    /**
     * Get average average ping time from last day
     * 
     * @param hostId
     * @return average average ping time from last day
     */
    public double getAverageAveragePingTimeFromLastDay(ObjectId hostId) {
        try {
            Document result = databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                    .aggregate(java.util.Arrays.asList(
                            Aggregates.match(Filters.and(
                                    Filters.eq("host_id", databaseEngine.getHostById(hostId).getHostIdMongo()),
                                    Filters.gt("ping_timestamp",
                                            new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                    Filters.lt("ping_timestamp", new Date(System.currentTimeMillis())))),
                            Aggregates.group(null, Accumulators.avg("avgPingTime", "$packet_round_trip_time_avg"))))
                    .first();

            if (result != null) {
                return result.getDouble("avgPingTime");
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
