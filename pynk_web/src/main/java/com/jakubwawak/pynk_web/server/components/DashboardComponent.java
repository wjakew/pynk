/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * DashboardComponent
 */
public class DashboardComponent extends VerticalLayout {

    HostAvgPingChartWrapper hostAvgPingChartWrapper1;

    /**
     * Constructor
     */
    public DashboardComponent() {
        addClassName("dashboard-component");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareLayout();
    }

    /**
     * Prepares the layout
     */
    private void prepareLayout() {

        hostAvgPingChartWrapper1 = new HostAvgPingChartWrapper(null, null, null);

        add(hostAvgPingChartWrapper1);
    }

}
