/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.database_engine;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.jakubwawak.pynk_web.entity.PingData;
import java.sql.PreparedStatement;

/**
 * Database data engine
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
            String sql = "SELECT * FROM ping_history WHERE ping_timestamp < ? AND ping_timestamp > ? ORDER BY ping_timestamp DESC";
            PreparedStatement preparedStatement = databaseEngine.getConnection().prepareStatement(sql);
            preparedStatement.setTimestamp(1, endDate);
            preparedStatement.setTimestamp(2, startDate);
            ResultSet resultSet = databaseEngine.executeSQLRead(preparedStatement);
            while (resultSet.next()) {
                pingData.add(new PingData(resultSet));
            }
            databaseEngine.addLog("DatabaseDataEngine", "Successfully got ping data between dates " + startDate
                    + " and " + endDate + " with " + pingData.size() + " rows", "INFO", "#00FF00");
            return pingData;
        } catch (SQLException e) {
            databaseEngine.addLog("DatabaseDataEngine", "Error getting ping data between dates " + e.getMessage(),
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
    public double getAverageAveragePingTimeFromLastDay(int hostId) {
        try {
            String sql = "SELECT AVG(packet_round_trip_time_avg) FROM ping_history WHERE host_id = ? AND ping_timestamp > ? AND ping_timestamp < ? ORDER BY ping_timestamp DESC";
            PreparedStatement preparedStatement = databaseEngine.getConnection().prepareStatement(sql);
            preparedStatement.setInt(1, hostId);
            preparedStatement.setTimestamp(2, new Timestamp(System.currentTimeMillis() - 24 * 60 * 60 * 1000));
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ResultSet resultSet = databaseEngine.executeSQLRead(preparedStatement);
            if (resultSet.next()) {
                return resultSet.getDouble("AVG(packet_round_trip_time_avg)");
            }
            return 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * Get ping data between dates
     * 
     * @param startDate
     * @param endDate
     * @return
     */

}
