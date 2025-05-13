/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.ping_engine;

import com.jakubwawak.Pynk;
import com.jakubwawak.entity.TraceSinglePath;
import com.jakubwawak.maintanance.ConsoleColors;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for generating trace route data
 */
public class TraceRouteEngine {
    
    /**
     * Executes traceroute command for a given host and returns list of hops
     * @param host target host to trace
     * @return List of TraceSinglePath objects representing each hop
     */
    public List<TraceSinglePath> executeTraceroute(String host) {
        ArrayList<TraceSinglePath> hops = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            // Use different command based on OS
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            if (isWindows) {
                processBuilder.command("tracert", host);
            } else {
                processBuilder.command("traceroute", host);
            }

            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            ArrayList<String> hops_raw = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                TraceSinglePath hop = parseTracerouteLine(line);
                if (hop != null) {
                    hops.add(hop);
                }
                hops_raw.add(line);
            }
            Pynk.documentDatabaseEngine.addTraceRouteData(hops, host);
            Pynk.documentDatabaseEngine.addLog("TRACE-ROUTE-DATA", "Added trace route data for host (" + host + ")", "INFO", ConsoleColors.GREEN);
            process.waitFor();
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return hops;
    }

    /**
     * Parses a single line of traceroute output
     * @param line line to parse
     * @return TraceSinglePath object if line contains valid hop data, null otherwise
     */
    private TraceSinglePath parseTracerouteLine(String line) {
        // Skip empty lines and header lines
        if (line.trim().isEmpty() || !line.matches(".*\\d+.*")) {
            return null;
        }

        try {
            // Extract hop number and hostname/IP
            Pattern pattern = Pattern.compile("\\s*(\\d+)\\s+(?:([\\w.-]+)\\s+)?\\((\\d+\\.\\d+\\.\\d+\\.\\d+)\\)\\s+([\\d.]+ ms\\s*[\\d.]+ ms\\s*[\\d.]+ ms)");
            Matcher matcher = pattern.matcher(line);

            if (matcher.find()) {
                String hopNumber = matcher.group(1);
                String hostname = matcher.group(2) != null ? matcher.group(2) : "";
                String ip = matcher.group(3);
                String[] times = matcher.group(4).split("\\s+ms\\s*");

                // Parse the time values
                double min = Double.MAX_VALUE;
                double max = 0;
                double sum = 0;
                int count = 0;

                for (String time : times) {
                    try {
                        double value = Double.parseDouble(time.trim());
                        min = Math.min(min, value);
                        max = Math.max(max, value);
                        sum += value;
                        count++;
                    } catch (NumberFormatException ignored) {
                        // Skip invalid time values
                    }
                }

                double avg = count > 0 ? sum / count : 0;
                return new TraceSinglePath(
                    hostname.isEmpty() ? "*" : hostname,
                    ip,
                    String.format("%.1f", max),
                    String.format("%.1f", min),
                    String.format("%.1f", avg)
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
