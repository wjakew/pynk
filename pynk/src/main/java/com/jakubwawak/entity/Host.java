/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bson.Document;
import org.bson.types.ObjectId;

/**
 * Host entity class
 */
public class Host {

    public int hostId; // Corresponds to host_id
    public ObjectId hostIdMongo; // Corresponds to host_id
    public String hostName; // Corresponds to host_name
    public String hostIp; // Corresponds to host_ip
    public String hostCategory; // Corresponds to host_category
    public String hostDescription; // Corresponds to host_description
    public String hostStatus; // Corresponds to host_status
    public int hostJobTime; // Corresponds to host_job_time

    /**
     * Constructor to create a host
     * 
     * @param hostId
     * @param hostName
     * @param hostIp
     * @param hostCategory
     * @param hostDescription
     */
    public Host(int hostId, String hostName, String hostIp, String hostCategory, String hostDescription,
            String hostStatus, int hostJobTime) {
        this.hostId = hostId;
        this.hostName = hostName;
        this.hostIp = hostIp;
        this.hostCategory = hostCategory;
        this.hostDescription = hostDescription;
        this.hostStatus = hostStatus;
        this.hostJobTime = hostJobTime; // in miliseconds
    }

    /**
     * Constructor to create a host from a document
     * 
     * @param document
     */
    public Host(Document document) {
        this.hostIdMongo = document.getObjectId("_id");
        this.hostName = document.getString("hostName");
        this.hostIp = document.getString("hostIp");
        this.hostCategory = document.getString("hostCategory");
        this.hostDescription = document.getString("hostDescription");
        this.hostStatus = document.getString("hostStatus");
        this.hostJobTime = document.getInteger("hostJobTime");
    }

    /**
     * Constructor to create a host from a result set
     * 
     * @param resultSet
     */
    public Host(ResultSet resultSet) {
        try {
            this.hostId = resultSet.getInt("host_id");
            this.hostName = resultSet.getString("host_name");
            this.hostIp = resultSet.getString("host_ip");
            this.hostCategory = resultSet.getString("host_category");
            this.hostDescription = resultSet.getString("host_description");
            this.hostStatus = resultSet.getString("host_status");
            this.hostJobTime = resultSet.getInt("host_job_time");
        } catch (SQLException e) {
            System.out.println("Error creating host from result set: " + e.getMessage());
        }
    }

    /**
     * Convert the host to a document
     * 
     * @return Document
     */
    public Document toDocument() {
        return new Document("_id", hostIdMongo)
                .append("hostName", hostName)
                .append("hostIp", hostIp)
                .append("hostCategory", hostCategory)
                .append("hostDescription", hostDescription)
                .append("hostStatus", hostStatus)
                .append("hostJobTime", hostJobTime);
    }

    /**
     * Get the host job time
     * 
     * @return int
     */
    public int getHostJobTime() {
        return hostJobTime;
    }

    // Getters and Setters
    public int getHostId() {
        return hostId;
    }

    public ObjectId getHostIdMongo() {
        return hostIdMongo;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(String hostStatus) {
        this.hostStatus = hostStatus;
    }

    public String getHostIp() {
        return hostIp;
    }

    public void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public String getHostCategory() {
        return hostCategory;
    }

    public void setHostCategory(String hostCategory) {
        this.hostCategory = hostCategory;
    }

    public String getHostDescription() {
        return hostDescription;
    }

    public void setHostDescription(String hostDescription) {
        this.hostDescription = hostDescription;
    }

    /**
     * Get the ping interval
     * 
     * @return int
     */
    public int getPingInterval() {
        return hostJobTime;
    }
}
