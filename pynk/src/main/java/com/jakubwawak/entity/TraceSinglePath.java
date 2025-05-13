/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.entity;

import org.bson.Document;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.codecs.pojo.annotations.BsonCreator;

/**
 * Object for storing trace route data
 */
public class TraceSinglePath {
    
    @BsonProperty("name")
    public String name;
    
    @BsonProperty("ip")
    public String ip;
    
    @BsonProperty("max")
    public String max;
    
    @BsonProperty("min")
    public String min;
    
    @BsonProperty("avg")
    public String avg;

    /**
     * No-args constructor required by MongoDB
     */
    public TraceSinglePath() {
        this.name = "Unknown";
        this.ip = "0.0.0.0";
        this.max = "0.0";
        this.min = "0.0";
        this.avg = "0.0";
    }

    /**
     * Constructor for TraceSinglePath
     * @param name
     * @param ip
     * @param max
     * @param min
     * @param avg
     */
    @BsonCreator
    public TraceSinglePath(String name, String ip, String max, String min, String avg) {
        this.name = name != null ? name : "Unknown";
        this.ip = ip != null ? ip : "0.0.0.0";
        this.max = max != null ? max : "0.0";
        this.min = min != null ? min : "0.0";
        this.avg = avg != null ? avg : "0.0";
        System.out.println("TraceSinglePath constructor: " + this.name + " " + this.ip + " " + this.max + " " + this.min + " " + this.avg);
    }

    // Getters and setters required by MongoDB
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getAvg() {
        return avg;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }
}
