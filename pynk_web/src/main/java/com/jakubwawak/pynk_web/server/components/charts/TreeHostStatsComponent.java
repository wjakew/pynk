/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.storedobject.chart.Orientation;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Title;
import com.storedobject.chart.TreeChart;
import com.storedobject.chart.TreeData;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;

import org.bson.Document;

/**
 * TreeHostStatsComponent
 */
public class TreeHostStatsComponent extends VerticalLayout {

    DatabaseDataEngine databaseDataEngine;

    /**
     * Constructor
     */
    public TreeHostStatsComponent() {
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        setSizeFull();
        
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareLayout();
    }

    /**
     * Prepare layout
     */
    public void prepareLayout() {
        removeAll();

        ArrayList<Document> hostStatistics = databaseDataEngine.getPublicHostStatistics();
        ArrayList<Document> localHostStatistics = databaseDataEngine.getLocalHostStatistics();

        SOChart soChart = new SOChart();
        soChart.setSizeFull();

        TreeChart tc = new TreeChart();

        TreeData rootData = new TreeData("Pynk", 0 );

        TreeData localData = new TreeData("Local", 1000);
        for( Document doc : localHostStatistics ) {
            localData.add(new TreeData(doc.getString("hostName"), doc.getDouble("avgPingTime")));
        }
        rootData.add(localData);

        TreeData internetData = new TreeData("Internet", databaseDataEngine.getAveragePacketRoundTripTimeFrom24hAllHosts());
        for (Document doc : hostStatistics) {
            if (doc.getString("hostName") != null && doc.getDouble("avgPingTime") != null) {
                internetData.add(new TreeData(doc.getString("hostName"), doc.getDouble("avgPingTime")));
            }
        }
        rootData.add(internetData);

        tc.setTreeData(rootData);

        Orientation orientation = tc.getOrientation(true);
        orientation.leftToRight();
        tc.setOrientation(orientation);

        soChart.add(tc, new Title("Your Hosts"));
        soChart.disableDefaultLegend();

        add(soChart);

    }
}
