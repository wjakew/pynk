package com.jakubwawak.ping_engine;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.jakubwawak.Pynk;
import com.jakubwawak.entity.Host;
import com.jakubwawak.entity.PingData;
import com.jakubwawak.database_engine.DocumentDatabaseEngine;

/**
 * MongoDB version of PingEngine class
 */
public class PingEngineDocument {

    private DocumentDatabaseEngine documentDatabaseEngine;

    public PingEngineDocument() {
        this.documentDatabaseEngine = Pynk.documentDatabaseEngine;
    }

    /**
     * Ping a host
     * 
     * @param host
     * @return PingData
     */
    public PingData pingHost(Host host) {
        PingData pingData = pingHostInternal(host.getHostIp(), 8);
        pingData.setHostId(host.getHostId());
        return pingData;
    }

    /**
     * Internal method to ping a host - should not be called directly
     * 
     * @param host
     * @param count
     * @return String
     */
    private PingData pingHostInternal(String host, int count) {
        PingData pingData = new PingData();
        String rawPing = "";
        documentDatabaseEngine.addLog("job", "Pinging host: " + host + " with " + count + " packets", "info",
                "#0000FF");
        try {
            ProcessBuilder pb = new ProcessBuilder("ping", "-c", String.valueOf(count), host);

            pingData.setTime(); // set ping timestamp
            Process process = pb.start();

            // read ping output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    rawPing += line + "\n";
                    if (line.contains("icmp_seq")) {
                        try {
                            String raw_index = line.split(" ")[4];
                            int index = Integer.parseInt(raw_index.substring(raw_index.indexOf("=") + 1));
                            pingData.setPacketHopTime(line.split(" ")[6].split("=")[1], index);
                        } catch (Exception e) {
                            documentDatabaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
                        }
                    } else if (line.contains("packets transmitted")) {
                        pingData.setPacketTransmitted(line.split(" ")[0]);
                        pingData.setPacketReceived(line.split(" ")[3]);
                    } else if (line.contains("round-trip") || line.contains("rtt")) {
                        String data = line.split("=")[1].stripLeading().stripTrailing();
                        pingData.setPacketRoundTripTimeMin(data.split("/")[0]);
                        pingData.setPacketRoundTripTimeMax(data.split("/")[2]);
                        pingData.setPacketRoundTripTimeAvg(data.split("/")[1]);
                    }
                }
            }
            if (pingData.verifyPacketHopTimes()) {
                documentDatabaseEngine.addLog("ping", "Ping successful to " + host + " with " + count + " packets",
                        "success", "#00FF00");
            } else {
                documentDatabaseEngine.addLog("ping", "Ping failed to " + host + " with " + count + " packets", "error",
                        "#FF0000");
                documentDatabaseEngine.addLog("ping-check", "Dig to " + host, "info", "#0000FF");
                pingData.setPacketDigData(digHost(host));
                documentDatabaseEngine.addLog("ping-check", "Traceroute to " + host, "info", "#0000FF");
                pingData.setPacketTracertData(traceHost(host));
            }
        } catch (Exception e) {
            documentDatabaseEngine.addLog("error", "Error: " + e.getMessage(), "error", "#FF0000");
        }
        pingData.setClassification();
        pingData.setPacketRawPing(rawPing);
        return pingData;
    }

    /**
     * Trace a host
     * 
     * @param host
     * @return String
     */
    private String traceHost(String host) {
        StringBuilder result = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("traceroute", "-m", "10", "-q", "1", "-w", "2", host);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            result.append("Exit Code: ").append(exitCode);
        } catch (Exception e) {
            result.append("Traceroute failed: ").append(e.getMessage());
        }

        return result.toString();
    }

    /**
     * Dig a host
     * 
     * @param hostname
     * @return String
     */
    private String digHost(String hostname) {
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("dig", hostname);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            output.append("Exit Code: ").append(exitCode).append("\n");

        } catch (Exception e) {
            output.append("dig failed: ").append(e.getMessage()).append("\n");
        }

        return output.toString();
    }
}
