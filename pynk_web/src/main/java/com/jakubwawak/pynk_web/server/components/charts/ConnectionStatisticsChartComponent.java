/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.PingData;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.LineChart;
import com.storedobject.chart.RectangularCoordinate;
import com.storedobject.chart.SOChart;
import com.storedobject.chart.Title;
import com.storedobject.chart.XAxis;
import com.storedobject.chart.YAxis;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.sql.Timestamp;

/**
 * ConnectionStatisticsChartComponent
 */
public class ConnectionStatisticsChartComponent extends VerticalLayout {

    DatabaseDataEngine databaseDataEngine;

    /**
     * Constructor
     */
    public ConnectionStatisticsChartComponent() {
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        prepareLayout();
    }

    /**
     * Prepare layout
     */
    private void prepareLayout() {
        removeAll();

        ArrayList<PingData> pingData = databaseDataEngine.getPingDataFromLastDay();
    
        SOChart soChart = new SOChart();
        soChart.setSize("100%", "100%");

        Data xValues = new Data(), yValues = new Data();

        for (PingData ping : pingData) {
            xValues.add(ping.pingTimestamp.getTime());
            if ( ping.packetStatusCode.equals("Success")) {
                yValues.add(1);
            } else if (ping.packetStatusCode.equals("No response")) {
                yValues.add(-1);
            } else {
                yValues.add(0);
            }
        }

        xValues.setName("Timestamp");
        yValues.setName("Connection Status");

        // Line chart is initialized with the generated XY values
        LineChart lineChart = new LineChart(xValues, yValues);
        lineChart.setName("1 - Success, -1 - No response, 0 - Partial Loss");

        // Line chart needs a coordinate system to plot on
        // We need Number-type for both X and Y axes in this case
        XAxis xAxis = new XAxis(DataType.DATE);
        YAxis yAxis = new YAxis(DataType.NUMBER);
        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        lineChart.plotOn(rc);

        // Add to the chart display area with a simple title
        soChart.add(lineChart, new Title("Connection Status ( last 24 hours )"));
        add(soChart);
    }
}
