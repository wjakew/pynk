/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.jakubwawak.Pynk;

/**
 * Ping data entity
 */
public class PingData {
    
    public int pingId;
    public int hostId;
    public Timestamp pingTimestamp;
    public String packetStatusCode;
    public String packetStatusColorHex;
    public int packetTransmitted;
    public int packetReceived;
    public double packetHopTime1;
    public double packetHopTime2;
    public double packetHopTime3;
    public double packetHopTime4;
    public double packetHopTime5;
    public double packetHopTime6;
    public double packetHopTime7;
    public double packetHopTime8;
    public String packetDigData;
    public String packetTracertData;

    public double packetRoundTripTimeMin;
    public double packetRoundTripTimeMax;
    public double packetRoundTripTimeAvg;

    public boolean error;

    /**
     * Default constructor
     */
    public PingData() {
        this.pingId = 0;
        this.hostId = 0;
        this.pingTimestamp = null;
        this.packetStatusCode = null;
        this.packetStatusColorHex = null;
        this.packetTransmitted = 0;
        this.packetReceived = 0;
        this.packetHopTime1 = -1;
        this.packetHopTime2 = -1;
        this.packetHopTime3 = -1;
        this.packetHopTime4 = -1;
        this.packetHopTime5 = -1;
        this.packetHopTime6 = -1;
        this.packetHopTime7 = -1;
        this.packetHopTime8 = -1;
        this.packetDigData = null;
        this.packetTracertData = null;
        this.packetRoundTripTimeMin = -1;
        this.packetRoundTripTimeMax = -1;
        this.packetRoundTripTimeAvg = -1;
    }

    /**
     * Constructor
     * @param pingId
     * @param hostId
     * @param pingTimestamp
     * @param packetStatusCode
     * @param packetStatusColorHex
     * @param packetTransmitted
     * @param packetReceived
     * @param packetHopTime1
     * @param packetHopTime2
     * @param packetHopTime3
     * @param packetHopTime4
     * @param packetHopTime5
     * @param packetHopTime6
     * @param packetHopTime7
     * @param packetHopTime8
     * @param packetDigData
     * @param packetTracertData
     */
    public PingData(int pingId, int hostId, Timestamp pingTimestamp, String packetStatusCode,
                    String packetStatusColorHex, int packetTransmitted, int packetReceived,
                    double packetHopTime1, double packetHopTime2, double packetHopTime3,
                    double packetHopTime4, double packetHopTime5, double packetHopTime6,
                    double packetHopTime7, double packetHopTime8, String packetDigData,
                    String packetTracertData, double packetRoundTripTimeMin,
                    double packetRoundTripTimeMax, double packetRoundTripTimeAvg) {
        this.pingId = pingId;
        this.hostId = hostId;
        this.pingTimestamp = pingTimestamp;
        this.packetStatusCode = packetStatusCode;
        this.packetStatusColorHex = packetStatusColorHex;
        this.packetTransmitted = packetTransmitted;
        this.packetReceived = packetReceived;
        this.packetHopTime1 = packetHopTime1;
        this.packetHopTime2 = packetHopTime2;
        this.packetHopTime3 = packetHopTime3;
        this.packetHopTime4 = packetHopTime4;
        this.packetHopTime5 = packetHopTime5;
        this.packetHopTime6 = packetHopTime6;
        this.packetHopTime7 = packetHopTime7;
        this.packetHopTime8 = packetHopTime8;
        this.packetDigData = packetDigData;
        this.packetTracertData = packetTracertData;
        this.packetRoundTripTimeMin = packetRoundTripTimeMin;
        this.packetRoundTripTimeMax = packetRoundTripTimeMax;
        this.packetRoundTripTimeAvg = packetRoundTripTimeAvg;
    }

    /**
     * Constructor
     * @param resultSet
     * @throws SQLException
     */
    public PingData(ResultSet resultSet) {
        try{
            this.pingId = resultSet.getInt("ping_id");
            this.hostId = resultSet.getInt("host_id");
            this.pingTimestamp = resultSet.getTimestamp("ping_timestamp");
            this.packetStatusCode = resultSet.getString("packet_status_code");
            this.packetStatusColorHex = resultSet.getString("packet_status_color_hex");
            this.packetTransmitted = resultSet.getInt("packet_transmitted");
            this.packetReceived = resultSet.getInt("packet_received");
            this.packetHopTime1 = resultSet.getDouble("packet_hop_time1");
            this.packetHopTime2 = resultSet.getDouble("packet_hop_time2");
            this.packetHopTime3 = resultSet.getDouble("packet_hop_time3");
            this.packetHopTime4 = resultSet.getDouble("packet_hop_time4");
            this.packetHopTime5 = resultSet.getDouble("packet_hop_time5");
            this.packetHopTime6 = resultSet.getDouble("packet_hop_time6");
            this.packetHopTime7 = resultSet.getDouble("packet_hop_time7");
            this.packetHopTime8 = resultSet.getDouble("packet_hop_time8");
            this.packetDigData = resultSet.getString("packet_dig_data");
            this.packetTracertData = resultSet.getString("packet_tracert_data");
            this.packetRoundTripTimeMin = resultSet.getDouble("packet_round_trip_time_min");
            this.packetRoundTripTimeMax = resultSet.getDouble("packet_round_trip_time_max");
            this.packetRoundTripTimeAvg = resultSet.getDouble("packet_round_trip_time_avg");
        } catch (SQLException e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Set ping timestamp
     */
    public void setTime(){
        this.pingTimestamp = new Timestamp(System.currentTimeMillis());
    }

    /**
     * Set packet transmitted
     * @param data
     */
    public void setPacketTransmitted(String data){
        try{
            this.packetTransmitted = Integer.parseInt(data);
        } catch (Exception e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Set packet received
     * @param data
     */
    public void setPacketReceived(String data){
        try{
            this.packetReceived = Integer.parseInt(data);
        } catch (Exception e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Set packet round trip time min
     * @param data
     */
    public void setPacketRoundTripTimeMin(String data){
        try{
            this.packetRoundTripTimeMin = Double.parseDouble(data);
        } catch (Exception e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Set packet round trip time max
     * @param data
     */
    public void setPacketRoundTripTimeMax(String data){
        try{
            this.packetRoundTripTimeMax = Double.parseDouble(data);
        } catch (Exception e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Set packet round trip time avg
     * @param data
     */
    public void setPacketRoundTripTimeAvg(String data){
        try{
            this.packetRoundTripTimeAvg = Double.parseDouble(data);
        } catch (Exception e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Verify packet hop times
     * @return boolean
     */
    public boolean verifyPacketHopTimes(){
        return this.packetHopTime1 != -1 && this.packetHopTime2 != -1 && this.packetHopTime3 != -1 &&
            this.packetHopTime4 != -1 && this.packetHopTime5 != -1 && this.packetHopTime6 != -1 &&
            this.packetHopTime7 != -1 && this.packetHopTime8 != -1;
    }

    /**
     * Has valid packet hop times
     * @return boolean
     */
    public boolean hasValidPacketHopTimes() {
        return this.packetHopTime1 == -1 || this.packetHopTime2 == -1 || this.packetHopTime3 == -1 ||
               this.packetHopTime4 == -1 || this.packetHopTime5 == -1 || this.packetHopTime6 == -1 ||
               this.packetHopTime7 == -1 || this.packetHopTime8 == -1;
    }

    /**
     * Set packet tracert data
     * @param data
     */
    public void setPacketTracertData(String data){
        this.packetTracertData = data;
    }

    /**
     * Set packet dig data
     * @param data
     */
    public void setPacketDigData(String data){
        this.packetDigData = data;
    }
    
    /**
     * Set packet hop time
     * @param data
     * @param index
     * @return int
     */
    public void setPacketHopTime(String data, int index) {
        try{
            double packetHopTime = Double.parseDouble(data);
            switch (index) {
                case 0:
                    this.packetHopTime1 = packetHopTime;
                    break;
                case 1:
                    this.packetHopTime2 = packetHopTime;
                    break;
                case 2:
                    this.packetHopTime3 = packetHopTime;
                    break;
                case 3:
                    this.packetHopTime4 = packetHopTime;
                    break;
                case 4:
                    this.packetHopTime5 = packetHopTime;
                    break;
                case 5:
                    this.packetHopTime6 = packetHopTime;
                    break;
                case 6:
                    this.packetHopTime7 = packetHopTime;
                    break;
                case 7:
                    this.packetHopTime8 = packetHopTime;
                    break;
            }
        } catch (Exception e) {
            this.error = true;
            Pynk.databaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
    }

    /**
     * Set classification
     */
    public void setClassification(){
        if ( this.packetReceived == 0 ){
            this.packetStatusCode = "No response";
            this.packetStatusColorHex = "#FF0000";
        } else if ( this.packetReceived < this.packetTransmitted ){
            this.packetStatusCode = "Partial loss";
            this.packetStatusColorHex = "#FFA500";
        } else {
            this.packetStatusCode = "Full loss";
            this.packetStatusColorHex = "#0000FF";
        }
        if ( this.packetReceived == this.packetTransmitted ){
            this.packetStatusCode = "Success";
            this.packetStatusColorHex = "#00FF00";
        }
    }
}
