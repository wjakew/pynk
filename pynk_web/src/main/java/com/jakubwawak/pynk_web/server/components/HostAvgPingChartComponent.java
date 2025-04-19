/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.Host;
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

/**
 * Component for presenting host chart
 */
public class HostAvgPingChartComponent extends VerticalLayout {

    Host host;
    Timestamp startDate;
    Timestamp endDate;

    /**
     * Constructor
     * 
     * @param host
     * @param startDate
     * @param endDate
     */
    public HostAvgPingChartComponent(Host host, Timestamp startDate, Timestamp endDate) {
        this.host = host;
        this.startDate = startDate;
        this.endDate = endDate;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareLayout();
    }

    /**
     * Prepare content
     */
    private SOChart prepareChart() {
        DatabaseDataEngine databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        ArrayList<PingData> pingData = databaseDataEngine.getPingDataBetweenDates(host, startDate, endDate);

        SOChart soChart = new SOChart();
        soChart.setSize("800px", "500px");

        Data xValues = new Data(), yValues = new Data();

        for (PingData ping : pingData) {
            xValues.add(ping.pingTimestamp.getTime());
            yValues.add(ping.getTimeAvg());
        }

        xValues.setName("Ping Timestamp");
        yValues.setName("AvgPing Time (ms)");

        // Line chart is initialized with the generated XY values
        LineChart lineChart = new LineChart(xValues, yValues);
        lineChart.setName("Ping Data for " + host.getHostName());

        // Line chart needs a coordinate system to plot on
        // We need Number-type for both X and Y axes in this case
        XAxis xAxis = new XAxis(DataType.NUMBER);
        YAxis yAxis = new YAxis(DataType.NUMBER);
        RectangularCoordinate rc = new RectangularCoordinate(xAxis, yAxis);
        lineChart.plotOn(rc);

        // Add to the chart display area with a simple title
        soChart.add(lineChart, new Title("Ping Data for " + host.getHostName()));

        return soChart;

    }

    /**
     * Prepare layout
     */
    private void prepareLayout() {
        add(prepareChart());
    }
}
