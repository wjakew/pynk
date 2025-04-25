/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.windows;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * Settings window
 */
public class SettingsWindow extends Dialog{

    VerticalLayout mainLayout;

    Checkbox allowPingHistoryDeletionCheckbox;

    DateTimePicker historyFrom, historyTo;

    Button deletePingHistoryButton;

    Grid<AppLog> appLogGrid;

    public SettingsWindow() {
        super();
        setHeaderTitle("Settings");
        setWidth("70%");
        setHeight("70&");

        mainLayout = new VerticalLayout();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        mainLayout.setPadding(true);
        mainLayout.setSpacing(true);
        mainLayout.setSizeFull();
        
        prepareLayout();
        
        add(mainLayout);
        
    }

    /**
     * Prepare layout
     */
    private void prepareLayout() {
        
    }
}

/**
 * AppLog class
 */
class AppLog {
    private String logCategory;
    private String logData;
    private String logCode;
    private String logColorHex;   
}
