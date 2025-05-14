/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components.charts;

import java.util.List;

import org.bson.Document;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.storedobject.chart.CategoryData;
import com.storedobject.chart.Data;
import com.storedobject.chart.FunnelChart;
import com.storedobject.chart.SOChart;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * TraceRouteChart - for showing trace route data
 */
public class TraceRouteChart extends VerticalLayout{

    DatabaseDataEngine databaseDataEngine;

    SOChart chart;
    FunnelChart funnelChart;


    /**
     * Constructor
     */
    public TraceRouteChart() {
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        prepareLayout();
    }

    /**
     * Prepare layout
     */
    void prepareLayout(){
        
        Document lastTraceRouteData = databaseDataEngine.getLastTraceRouteData();

        if(lastTraceRouteData == null){
            add(new Text("No trace route data found"));
            return;
        }

        List<Document> traceRouteData = lastTraceRouteData.get("hops", List.class);

        CategoryData hosts = new CategoryData();
        Data data = new Data();

        int i = 0;
        for(Document hop : traceRouteData){
            String host = hop.get("name", String.class);
            String avg = hop.get("avg", String.class);
            hosts.add(host);
            data.add(Double.parseDouble(avg));
            i++;
        }

        funnelChart = new FunnelChart(hosts, data);

        chart = new SOChart();
        chart.add(funnelChart);
        
        chart.setSizeFull();

        chart.disableDefaultLegend();

        add(new H6("Trace Route"));
        add(chart);
    }    

}
