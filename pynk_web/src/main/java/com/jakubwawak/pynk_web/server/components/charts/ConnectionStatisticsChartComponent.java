/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.PingData;
import com.storedobject.chart.Color;
import com.storedobject.chart.ColorGradient;
import com.storedobject.chart.Data;
import com.storedobject.chart.DataType;
import com.storedobject.chart.LineChart;
import com.storedobject.chart.LineStyle;
import com.storedobject.chart.PointSymbol;
import com.storedobject.chart.PointSymbolType;
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
            if (ping.packetStatusCode.equals("Success")) {
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

        LineStyle lineStyle = lineChart.getLineStyle(true);
        lineStyle.setColor(new Color("red"));
        lineStyle.setWidth(4);
        ColorGradient cg = new ColorGradient(new Color("pink"), new Color("white"));
        cg.setGradient(0, 0, 100, 100);
        lineChart.getAreaStyle(true).setColor(cg);
        PointSymbol ps = lineChart.getPointSymbol(true);
        ps.setType(PointSymbolType.NONE);
        ps.setSize(15, 15);
        lineChart.setColors(new Color("pink")); // Data-points should be in black
        lineChart.setSmoothness(100); // Make it very smooth

        lineChart.setName("1 - Success, 0 - Partial Loss, -1 - No response");

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
