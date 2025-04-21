/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Minus.Horizontal;

/**
 * StatisticsComponent
 */
public class StatisticsComponent extends VerticalLayout {

    DatabaseDataEngine databaseDataEngine;

    HorizontalLayout topLayout;

    /**
     * Constructor
     */
    public StatisticsComponent() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

}
