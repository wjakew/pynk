/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.jakubwawak.pynk_web.server.components.charts.TreeHostStatsComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * HostsDashboardComponent - for showing hosts dashboard
 */
public class HostsDashboardComponent extends VerticalLayout {

    /**
     * Constructor
     */
    public HostsDashboardComponent() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        prepareLayout();
    }

    /**
     * Prepare layout
     */
    void prepareLayout(){
        TreeHostStatsComponent treeHostStatsComponent = new TreeHostStatsComponent();
        add(treeHostStatsComponent);
    }

}