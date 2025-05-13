/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import com.jakubwawak.entity.TraceSinglePath;
import com.jakubwawak.ping_engine.TraceRouteEngine;
import java.util.List;

/**
 * PynkTest class
 */
public class PynkTest {

    /**
     * Run the test
     */
    void run() {
        System.out.println("PynkTest");
        TraceRouteEngine tre = new TraceRouteEngine();
        List<TraceSinglePath> hops = tre.executeTraceroute("8.8.8.8");
        for (TraceSinglePath hop : hops) {
            System.out.println(hop.name + " " + hop.ip + " " + hop.max + " " + hop.min + " " + hop.avg);
        }
    }
}
