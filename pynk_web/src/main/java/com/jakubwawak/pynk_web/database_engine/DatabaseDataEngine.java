/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.database_engine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jakubwawak.pynk_web.entity.Host;
import com.jakubwawak.pynk_web.entity.PingData;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;

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

                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Successfully got ping data between dates " + startDate
                                                        + " and " + endDate + " with " + pingData.size() + " rows",
                                        "INFO", "#00FF00");
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting ping data between dates " + e.getMessage(),
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

                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Successfully got ping data between dates " + startDate
                                                        + " and " + endDate + " with " + pingData.size() + " rows",
                                        "INFO", "#00FF00");
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting ping data between dates " + e.getMessage(),
                                        "ERROR", "#FF0000");
                        return null;
                }
        }

        /**
         * Remove ping data between dates
         * 
         * @param startDate
         * @param endDate
         * @return true if successful, false otherwise
         */
        public int removePingDataBetweenDates(Timestamp startDate, Timestamp endDate) {
                try {
                        DeleteResult result =databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                                        .deleteMany(Filters.and(
                                                        Filters.gt("ping_timestamp", new Date(startDate.getTime())),
                                                        Filters.lt("ping_timestamp", new Date(endDate.getTime()))));
                        return (int) result.getDeletedCount();
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error removing ping data between dates " + e.getMessage(),
                                        "ERROR", "#FF0000");
                        return -1;
                }
        }

        /**
         * Remove ping data older than 3 months ago
         * 
         * @return true if successful, false otherwise
         */
        public int removePingOlderThan3MonthsAgo() {
                try {
                        DeleteResult result = databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION)
                                        .deleteMany(Filters.lt("ping_timestamp", new Date(System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000)));
                        return (int) result.getDeletedCount();
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error removing ping data older than 3 months ago " + e.getMessage(),
                                        "ERROR", "#FF0000");
                        return -1;
                }
        }
        /**
         * Get number of successes from last 24 hours
         * 
         * @return number of successes from last 24 hours
         */
        public int getNumberOfSuccessesFrom24h() {
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        int count = 0;
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {

                                if (doc.getString("packet_status_code").equals("Success")) {
                                        count++;
                                }
                        }
                        return count;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting number of successes from last 24 hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return 0;
                }
        }

        /**
         * Get partial loss in hours
         * 
         * @param hours
         * @return partial loss in hours
         */
        public ArrayList<PingData> getPartialLossInHours(int hours) {
                ArrayList<PingData> pingData = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - hours * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {
                                if (doc.getString("packet_status_code").equals("Partial loss")) {
                                        pingData.add(new PingData(doc));
                                }
                        }
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting partial loss in hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
                }
        }

        /**
         * Get number of failures from last 24 hours
         * 
         * @return number of failures from last 24 hours
         */
        public int getNumberOfNoResponseFrom24h() {
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        int count = 0;
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {

                                if (doc.getString("packet_status_code").equals("No response")) {
                                        count++;
                                }
                        }
                        return count;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting number of no response from last 24 hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return 0;
                }
        }

        /**
         * Get no response in hours
         * 
         * @param hours
         * @return no response in hours
         */
        public ArrayList<PingData> getNoResponseInHours(int hours) {
                ArrayList<PingData> pingData = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine.getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - hours * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {
                                if (doc.getString("packet_status_code").equals("No response")) {
                                        pingData.add(new PingData(doc));
                                }
                        }
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting no response in hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
                }
        }

                /**
         * Get number of partial loss from last 24 hours
         * 
         * @return number of partial loss from last 24 hours
         */
        public int getNumberOfPartialLossFrom24h() {
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        int count = 0;
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {

                                if (doc.getString("packet_status_code").equals("Partial loss")) {
                                        count++;
                                }
                        }
                        return count;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting number of failures from last 24 hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return 0;
                }
        }

        /**
         * Get failures from last 24 hours
         * 
         * @return failures from last 24 hours
         */
        public ArrayList<PingData> getFailuresFrom24h() {
                ArrayList<PingData> pingData = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))
                                        .sort(Sorts.descending("ping_timestamp"))) {

                                if (!doc.getString("packet_status_code").equals("Success")) {
                                        pingData.add(new PingData(doc));
                                }
                        }
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting failures from last 24 hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
                }
        }
        /**
         * Get number of hosts
         * 
         * @return number of hosts
         */
        public int getNumberOfHosts() {
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.HOSTS_COLLECTION);
                        return (int) collection.countDocuments();
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting number of hosts ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return 0;
                }
        }

        /**
         * Get average packet round trip time from last 24 hours for given host
         * 
         * @param hostId host id to get statistics for
         * @return average packet round trip time
         */
        public double getAveragePacketRoundTripTimeFrom24h(ObjectId hostId) {
            try {
                MongoCollection<Document> collection = databaseEngine
                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                
                List<Document> results = collection.find(Filters.and(
                        Filters.eq("host_id", hostId),
                        Filters.gt("ping_timestamp", new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))
                )).into(new ArrayList<>());

                if (results.isEmpty()) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                "No data found for host " + hostId,
                                "INFO", "#00FF00");
                    return 0.0;
                }

                double sum = 0.0;
                int count = 0;
                for (Document doc : results) {
                    Double roundTripTime = doc.getDouble("packet_round_trip_time_avg");
                    if (roundTripTime != null) {
                        sum += roundTripTime;
                        count++;
                    }
                }
                databaseEngine.addLog("DatabaseDataEngine",
                        "Average packet round trip time from last 24 hours for host " + hostId + " is " + sum / count,
                        "INFO", "#00FF00");
                return count > 0 ? sum / count : 0.0;
            } catch (Exception e) {
                databaseEngine.addLog("DatabaseDataEngine",
                        "Error getting average packet round trip time from last 24 hours ("
                                + e.getMessage() + ")",
                        "ERROR", "#FF0000");
                return 0.0;
            }
        }

        /**
         * Get average packet round trip time from last 24 hours for all hosts
         * 
         * @return average packet round trip time
         */
        public double getAveragePacketRoundTripTimeFrom24hAllHosts() {
            try {
                MongoCollection<Document> collection = databaseEngine
                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                
                List<Document> results = collection.find(Filters.and(
                        Filters.gt("ping_timestamp", new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))
                )).into(new ArrayList<>());

                if (results.isEmpty()) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                "No data found for any host in last 24 hours",
                                "INFO", "#00FF00");
                    return 0.0;
                }

                double sum = 0.0;
                int count = 0;
                for (Document doc : results) {
                    Double roundTripTime = doc.getDouble("packet_round_trip_time_avg");
                    if (roundTripTime != null) {
                        sum += roundTripTime;
                        count++;
                    }
                }
                databaseEngine.addLog("DatabaseDataEngine",
                        "Average packet round trip time from last 24 hours for all hosts is " + sum / count,
                        "INFO", "#00FF00");
                return count > 0 ? sum / count : 0.0;
            } catch (Exception e) {
                databaseEngine.addLog("DatabaseDataEngine",
                        "Error getting average packet round trip time from last 24 hours for all hosts ("
                                + e.getMessage() + ")",
                        "ERROR", "#FF0000");
                return 0.0;
            }
        }

        /**
         * Get host statistics
         * 
         * @return host statistics
         */
        public ArrayList<Document> getPublicHostStatistics() {
                ArrayList<Document> hostStatistics = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine.getCollection("hosts");
                        for (Document doc : collection.find()) {
                                if ( doc.getString("hostCategory").equals("public")) {
                                        ObjectId hostId = doc.getObjectId("_id");
                                        String hostName = doc.getString("hostName");
                                        double avgPingTime = getAveragePacketRoundTripTimeFrom24h(hostId);
                                        hostStatistics.add(new Document("hostName", hostName).append("avgPingTime", avgPingTime));
                                }
                        }
                        return hostStatistics;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting host statistics ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
                }
        }

                /**
         * Get host statistics
         * 
         * @return host statistics
         */
        public ArrayList<Document> getLocalHostStatistics() {
                ArrayList<Document> hostStatistics = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine.getCollection("hosts");
                        for (Document doc : collection.find()) {
                                if ( doc.getString("hostCategory").equals("local")) {
                                        ObjectId hostId = doc.getObjectId("_id");
                                        String hostName = doc.getString("hostName");
                                        double avgPingTime = getAveragePacketRoundTripTimeFrom24h(hostId);
                                        hostStatistics.add(new Document("hostName", hostName).append("avgPingTime", avgPingTime));
                                }
                        }
                        return hostStatistics;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting host statistics ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
                }
        }

        /**
         * Get ping data from last day
         * 
         * @return ping data from last day
         */
        public ArrayList<PingData> getPingDataFromLastDay() {
                ArrayList<PingData> pingData = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {
                                pingData.add(new PingData(doc));
                        }
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting ping data from last day ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
                }
        }

        /**
         * Get ping data from last hours
         * 
         * @param hours
         * @return ping data from last hours
         */
        public ArrayList<PingData> getPingFromHoursAgo(int hours) {
                ArrayList<PingData> pingData = new ArrayList<>();
                try {
                        MongoCollection<Document> collection = databaseEngine
                                        .getCollection(DatabaseEngine.PING_HISTORY_COLLECTION);
                        for (Document doc : collection.find(Filters.and(
                                        Filters.gt("ping_timestamp",
                                                        new Date(System.currentTimeMillis() - hours * 60 * 60 * 1000)),
                                        Filters.lt("ping_timestamp", new Date(System.currentTimeMillis()))))) {
                                pingData.add(new PingData(doc));
                        }
                        databaseEngine.addLog("DatabaseDataEngine",
                                "Successfully got ping data from last " + hours + " hours ("
                                        + pingData.size() + " rows)",
                                "INFO", "#00FF00");
                        return pingData;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                        "Error getting ping data from last " + hours + " hours ("
                                                        + e.getMessage() + ")",
                                        "ERROR", "#FF0000");
                        return null;
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
                                                                        Filters.eq("host_id", databaseEngine
                                                                                        .getHostById(hostId)
                                                                                        .getHostIdMongo()),
                                                                        Filters.gt("ping_timestamp",
                                                                                        new Date(System.currentTimeMillis()
                                                                                                        - 24 * 60 * 60 * 1000)),
                                                                        Filters.lt("ping_timestamp", new Date(
                                                                                        System.currentTimeMillis())))),
                                                        Aggregates.group(null, Accumulators.avg("avgPingTime",
                                                                        "$packet_round_trip_time_avg"))))
                                        .first();

                        if (result != null) {
                                return result.getDouble("avgPingTime");
                        }
                        return 0;
                } catch (Exception e) {
                        return 0;
                }
        }

        /**
         * Get last trace route data
         * 
         * @return last trace route data
         */
        public Document getLastTraceRouteData(){
                try{
                        MongoCollection<Document> collection = databaseEngine.getCollection("trace_route_data");
                        Document result = collection.find().sort(Sorts.descending("timestamp")).first();
                        databaseEngine.addLog("DatabaseDataEngine",
                                "Successfully got last trace route data ("
                                        + result.get("timestamp") + ")",
                                "INFO", "#00FF00");
                        return result;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                "Error getting last trace route data ("
                                        + e.getMessage() + ")",
                                "ERROR", "#FF0000");
                        return null;
                }
        }

        /**
         * Get last hourly ping data
         * 
         * @return last hourly ping data
         */
        public Document getLastHourlyPingData(){
                try{
                        MongoCollection<Document> collection = databaseEngine.getCollection("hour_default_ping_data");
                        Document result = collection.find().sort(Sorts.descending("ping_timestamp")).first();
                        databaseEngine.addLog("DatabaseDataEngine",
                                "Successfully got last hourly ping data ("
                                        + result.get("ping_timestamp") + ")",
                                "INFO", "#00FF00");
                        Document parsedResult = new Document();
                        parsedResult.append("ping_timestamp", result.get("ping_timestamp"));
                        parsedResult.append("avg", result.get("ping_avg"));
                        parsedResult.append("min", result.get("ping_min"));
                        parsedResult.append("max", result.get("ping_max"));
                        return parsedResult;
                } catch (Exception e) {
                        databaseEngine.addLog("DatabaseDataEngine",
                                "Error getting last hourly ping data ("
                                        + e.getMessage() + ")",
                                "ERROR", "#FF0000");
                        return null;
                }
        }

        /**
         * Get average ping data from the last day
         * 
         * @return Document containing average, min, and max ping data from the last day
         */
        public Document getAveragePingDataFromLastDay() {
        try {
                MongoCollection<Document> collection = databaseEngine.getCollection("hour_default_ping_data");
                List<Document> results = collection.find()
                .sort(Sorts.descending("ping_timestamp"))
                .limit(24) // Assuming we want the last 24 hours
                .into(new ArrayList<>());

                double totalAvg = 0;
                double totalMin = Double.MAX_VALUE;
                double totalMax = Double.MIN_VALUE;
                int count = 0;

                for (Document result : results) {
                        double avg = result.getDouble("ping_avg");
                        double min = result.getDouble("ping_min");
                        double max = result.getDouble("ping_max");

                        totalAvg += avg;
                        totalMin = Math.min(totalMin, min);
                        totalMax = Math.max(totalMax, max);
                        count++;
                }

                if (count > 0) {
                totalAvg /= count; // Calculate average of averages
                } else {
                totalAvg = 0; // No data case
                totalMin = 0;
                totalMax = 0;
                }

                Document parsedResult = new Document();
                parsedResult.append("avg", totalAvg);
                parsedResult.append("min", totalMin);
                parsedResult.append("max", totalMax);
                return parsedResult;

        } catch (Exception e) {
                databaseEngine.addLog("DatabaseDataEngine",
                        "Error getting average ping data from last day ("
                                + e.getMessage() + ")",
                        "ERROR", "#FF0000");
                return null;
        }
        }


        /**
         * Function for loading last average data
         * @return
         */
        public double getLastAveragePingData(){
        try {
            MongoCollection<Document> collection = databaseEngine.getCollection("hour_default_ping_data");
            Document lastRecord = collection.find()
                .sort(Sorts.descending("ping_timestamp"))
                .first(); // Get the most recent record

                if (lastRecord != null) {
                        return lastRecord.getDouble("ping_avg"); // Return the average ping from the last record
                } else {
                        return 0; // No data case
                }

        } catch (Exception e) {
        databaseEngine.addLog("DatabaseDataEngine",
                "Error getting last average ping data ("
                        + e.getMessage() + ")",
                "ERROR", "#FF0000");
        return 0; // Return 0 in case of error
        }
        }
}

