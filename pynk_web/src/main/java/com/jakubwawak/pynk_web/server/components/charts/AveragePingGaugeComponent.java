/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.storedobject.chart.Color;
import com.storedobject.chart.Font;
import com.storedobject.chart.GaugeChart;
import com.storedobject.chart.Label;
import com.storedobject.chart.SOChart;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * AveragePingGaugeComponent
 */
public class AveragePingGaugeComponent extends VerticalLayout{

    DatabaseDataEngine databaseDataEngine;

    SOChart soChart;

    GaugeChart gaugeChart;

    /**
     * Constructor
     */
    public AveragePingGaugeComponent(){
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        prepareLayout();
    }

    /**
     * Function for preparing layout
     */
    void prepareLayout(){
        soChart = new SOChart();
        soChart.setSizeFull();

        gaugeChart = new GaugeChart();
        gaugeChart.setMin(0);
        gaugeChart.setMax(150);

        gaugeChart.setValue(databaseDataEngine.getLastAveragePingData());
        gaugeChart.showProgress(true);
        
        
        Label label = gaugeChart.getAxisLabel(true);
        label.setFontStyle(new Font(null, Font.Size.number(15)));
        label.setFormatter("{value} ms");

        gaugeChart.addDialZone(30, new Color("green"));
        gaugeChart.addDialZone(50, new Color("yellow"));
        gaugeChart.addDialZone(100, new Color("red"));

        soChart.add(gaugeChart);
        soChart.disableDefaultLegend();

        add(new H6("Current Average Ping"));
        add(soChart);
    }
    
}
