/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.server.components.charts.AveragePingGaugeComponent;
import com.jakubwawak.pynk_web.server.components.charts.ConnectionStatisticsChartComponent;
import com.jakubwawak.pynk_web.server.components.charts.HostAvgPingChartComponent;
import com.jakubwawak.pynk_web.server.components.charts.PingRadarChart;
import com.jakubwawak.pynk_web.server.components.charts.TraceRouteChart;
import com.jakubwawak.pynk_web.server.components.charts.TreeHostStatsComponent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Minus.Horizontal;

/**
 * StatisticsComponent
 */
public class StatisticsComponent extends VerticalLayout {

    DatabaseDataEngine databaseDataEngine;

    HorizontalLayout topLayout;

    HorizontalLayout centerLayout;

    /**
     * Constructor
     */
    public StatisticsComponent() {
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareTopLayout();
        prepareCenterLayout();

        prepareLayout();
    }

    /**
     * Prepare top layout
     */
    void prepareTopLayout() {
        topLayout = new HorizontalLayout();
        topLayout.setAlignItems(Alignment.CENTER);
        topLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        topLayout.setWidthFull();

        FlexLayout leftLayout = new FlexLayout();
        leftLayout.setSizeFull();
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setWidthFull();

        FlexLayout rightLayout = new FlexLayout();
        rightLayout.setSizeFull();
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setWidthFull();

        int successes = databaseDataEngine.getNumberOfSuccessesFrom24h();
        int noResponse = databaseDataEngine.getNumberOfNoResponseFrom24h();
        int partialLoss = databaseDataEngine.getNumberOfPartialLossFrom24h();

        H6 statusHeader = new H6();

        if (successes > 0 && noResponse == 0 && partialLoss == 0) {
            statusHeader.setText("Perfect Connectivity");
        } else if (successes > 0 && noResponse > 0 && partialLoss == 0) {
            statusHeader.setText("Need attention");
            statusHeader.getStyle().set("color", "#FFA500");
        } else if (successes > 0 && noResponse > 0 && partialLoss > 0) {
            statusHeader.setText("Warning");
            statusHeader.getStyle().set("color", "#FF0000");
        } else {
            statusHeader.setText("We are losing");
            statusHeader.getStyle().set("color", "#FF0000");
        }

        leftLayout.add(new H6("Status: "), statusHeader);

        rightLayout.add(new H6("Hosts: " + databaseDataEngine.getNumberOfHosts()));

        topLayout.add(leftLayout);
        topLayout.add(rightLayout);
    }

    /**
     * Prepare center layout
     */
    void prepareCenterLayout() {
        centerLayout = new HorizontalLayout();
        centerLayout.setAlignItems(Alignment.CENTER);
        centerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        centerLayout.setWidthFull();
        centerLayout.setHeight("60%");

        ConnectionStatisticsChartComponent connectionStatisticsChartComponent = new ConnectionStatisticsChartComponent();
        centerLayout.add(connectionStatisticsChartComponent);
    }

    /**
     * Prepare layout
     */
    public void prepareLayout() {
        removeAll();
        add(topLayout);
        prepareCenterLayout();
        add(centerLayout);

        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setAlignItems(Alignment.CENTER);
        bottomLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        bottomLayout.setWidthFull();
        bottomLayout.setHeight("40%");


        TraceRouteChart traceRouteChart = new TraceRouteChart();
        bottomLayout.add(traceRouteChart);

        PingRadarChart pingRadarChart = new PingRadarChart();
        bottomLayout.add(pingRadarChart);

        AveragePingGaugeComponent averagePingGaugeComponent = new AveragePingGaugeComponent();
        bottomLayout.add(averagePingGaugeComponent);

        add(bottomLayout);

        Notification.show("StatisticsComponent updated!");
    }

}
