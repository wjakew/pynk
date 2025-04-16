/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.entity;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Host entity class
 */
public class Host {
    private int hostId; // Corresponds to host_id
    private String hostName; // Corresponds to host_name
    private String hostIp; // Corresponds to host_ip
    private String hostCategory; // Corresponds to host_category
    private String hostDescription; // Corresponds to host_description

    /**
     * Constructor to create a host
     * @param hostId
     * @param hostName
     * @param hostIp
     * @param hostCategory
     * @param hostDescription
     */
    public Host(int hostId, String hostName, String hostIp, String hostCategory, String hostDescription) {
        this.hostId = hostId;
        this.hostName = hostName;
        this.hostIp = hostIp;
        this.hostCategory = hostCategory;
        this.hostDescription = hostDescription;
    }

    /**
     * Constructor to create a host from a result set
     * @param resultSet
     */
    public Host(ResultSet resultSet) {
        try {
            this.hostId = resultSet.getInt("host_id");
            this.hostName = resultSet.getString("host_name");
            this.hostIp = resultSet.getString("host_ip");
            this.hostCategory = resultSet.getString("host_category");
            this.hostDescription = resultSet.getString("host_description");
        } catch (SQLException e) {
            System.out.println("Error creating host from result set: " + e.getMessage());
        }
    }

    // Getters and Setters
    public int getHostId() {
        return hostId;
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
}
