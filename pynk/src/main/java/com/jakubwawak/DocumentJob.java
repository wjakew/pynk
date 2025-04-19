/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import com.jakubwawak.database_engine.DocumentDatabaseEngine;
import com.jakubwawak.entity.Host;
import com.jakubwawak.entity.PingData;
import com.jakubwawak.ping_engine.PingEngineDocument;

/**
 * Job class for document database - for creating ping pipeline for every host
 */
public class DocumentJob {

    private DocumentDatabaseEngine documentDatabaseEngine;
    private Host host;

    /**
     * Constructor for the DocumentJob class
     */
    public DocumentJob(Host host) {
        this.host = host;
        this.documentDatabaseEngine = Pynk.documentDatabaseEngine;
    }

    /**
     * Run the job
     */
    public void run() {
        try {
            documentDatabaseEngine.log("job", "Starting job for host: " + host.getHostName());
            documentDatabaseEngine.addHostLog(host.hostId, "job", "Starting job for host: " + host.getHostName(),
                    "info",
                    "#0000FF");
            PingEngineDocument pingEngine = new PingEngineDocument();
            PingData pingData = pingEngine.pingHost(host);
            pingData.hostIdMongo = host.hostIdMongo;
            documentDatabaseEngine.addPingData(pingData);
            documentDatabaseEngine.log("job", "Job for host: " + host.getHostName() + " completed,status: "
                    + pingData.getPacketStatusCode() + " waiting for " + host.getPingInterval() + " seconds");
            documentDatabaseEngine.addHostLog(host.hostId, "job",
                    "Job for host: " + host.getHostName() + " completed,status: " + pingData.getPacketStatusCode()
                            + " waiting for " + host.getPingInterval() + " seconds",
                    "info",
                    "#0000FF");
        } catch (Exception e) {
            documentDatabaseEngine.log("job", "Error in job for host: " + host.getHostName() + " - " + e.getMessage());
            documentDatabaseEngine.addHostLog(host.hostId, "job-error",
                    "Error in job for host: " + host.getHostName() + " - " + e.getMessage(), "error", "#FF0000");
        }
    }

}
