/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.RadarChart;
import com.storedobject.chart.RadarCoordinate;
import com.storedobject.chart.SOChart;

import org.bson.Document;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * PingRadarChart - a chart that displays the ping data in a radar chart
 */
public class PingRadarChart extends VerticalLayout {

    DatabaseDataEngine databaseDataEngine;

    SOChart sochart;

    /**
     * Constructor
     */
    public PingRadarChart() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        prepareLayout();
    }

    /**
     * Prepare the layout
     */
    private void prepareLayout() {

        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);

        Document lastHourlyPingData = databaseDataEngine.getLastHourlyPingData();
        Document lastDayPingData = databaseDataEngine.getAveragePingDataFromLastDay();

        if(lastHourlyPingData == null || lastDayPingData == null){
            add(new Text("No ping data found"));
            return;
        }

        CategoryData legs = new CategoryData("Hour Average","Day Average");

        Data lastHourAverage = new Data(lastHourlyPingData.getDouble("avg"), lastHourlyPingData.getDouble("min"), lastHourlyPingData.getDouble("max"));
        Data lastDayAverage = new Data(lastDayPingData.getDouble("avg"), lastDayPingData.getDouble("min"), lastDayPingData.getDouble("max"));

        lastHourAverage.setName("Last Hour");
        lastDayAverage.setName("Last Day");

        RadarChart chart = new RadarChart();
        chart.addData(lastHourAverage);

        chart.addData(lastDayAverage);
        
        RadarCoordinate radarCoordinate = new RadarCoordinate(legs);
        radarCoordinate.setCircular(true);
        
        chart.plotOn(radarCoordinate);

        sochart = new SOChart();
        sochart.setSizeFull();

        sochart.disableDefaultLegend();
        
        sochart.add(chart);

        add(sochart);
    }
}
