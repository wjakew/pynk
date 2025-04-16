/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import com.jakubwawak.database_engine.DatabaseEngine;
import com.jakubwawak.entity.Host;
import com.jakubwawak.entity.PingData;
import com.jakubwawak.ping_engine.PingEngine;

/**
 * Job class - for creating ping pipeline for every host
 */
public class Job {

    private DatabaseEngine databaseEngine;
    private Host host;

    /**
     * Constructor for the Job class
     */
    public Job(Host host){
        this.host = host;
    }

    /**
     * Run the job
     */
    public void run(){
        this.databaseEngine = new DatabaseEngine(Pynk.properties.getValue("databasePath"));
        databaseEngine.connect();
        Pynk.databaseEngine.addLog("job", "Starting job for host: " + host.getHostName(), "info", "#0000FF");
        PingEngine pingEngine = new PingEngine();
        PingData pingData = pingEngine.pingHost(host);
        Pynk.databaseEngine.addPingData(pingData);
        Pynk.databaseEngine.addLog("job", "Job for host: " + host.getHostName() + " completed", "info", "#0000FF");
        Pynk.databaseEngine.closeConnection();
    }
    
}
