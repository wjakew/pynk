/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.jakubwawak.pynk_web.entity.PingData;
import com.storedobject.chart.SOChart;

import java.util.ArrayList;

/**
 * LostPingChart - chart for lost pings
 */
public class LostPingChart extends VerticalLayout{

    SOChart soChart;

    DatabaseDataEngine databaseDataEngine;
    
    /**
     * Constructor
     */
    public LostPingChart(){
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        prepareLayout();
    }

    /**
     * Prepare layout
     */
    private void prepareLayout(){

        ArrayList<PingData> failures = databaseDataEngine.getFailuresFrom24h();

        if(failures == null){
            add(new Text("No data available"));
            return;
        }
        
    }
}
