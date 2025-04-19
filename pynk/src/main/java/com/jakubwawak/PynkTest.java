/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak;

import com.jakubwawak.entity.Host;

/**
 * PynkTest class
 */
public class PynkTest {

    /**
     * Run the test
     */
    void run() {
        System.out.println("PynkTest");
        Host host = new Host(1, "Test Host", "127.0.0.1", "Test Category", "Test Description", "active", 30000);
        DocumentJob documentJob = new DocumentJob(host);
        documentJob.run();
    }
}
