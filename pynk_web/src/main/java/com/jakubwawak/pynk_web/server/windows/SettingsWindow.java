/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.windows;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;

/**
 * Settings window
 */
public class SettingsWindow extends Dialog{

    VerticalLayout mainLayout;

    Checkbox allowPingHistoryDeletionCheckbox;

    DatePicker historyFrom, historyTo;

    Button deletePingHistoryButton;

    Grid<AppLog> appLogGrid;

    DatabaseEngine databaseEngine;
    DatabaseDataEngine databaseDataEngine;

    public SettingsWindow() {
        super();
        databaseEngine = PynkWebApplication.databaseEngine;
        databaseDataEngine = new DatabaseDataEngine(databaseEngine);
        setHeaderTitle("Settings");
        setWidth("50%");
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
        
        allowPingHistoryDeletionCheckbox = new Checkbox("Allow ping history deletion");
        allowPingHistoryDeletionCheckbox.setValue(databaseEngine.getConfigurationAllowPingHistoryDeletion());
        allowPingHistoryDeletionCheckbox.addValueChangeListener(e -> {
            databaseEngine.setConfigurationAllowPingHistoryDeletion(e.getValue());
            if ( e.getValue() == false){
                Notification.show("Ping history deletion is disabled");
            }
            else{
                Notification.show("Ping history deletion is enabled");
            }
        });

        historyFrom = new DatePicker("Date From");
        historyFrom.setValue(LocalDate.now().minusDays(90));

        historyTo = new DatePicker("Date To");
        historyTo.setValue(LocalDate.now().minusDays(30));

        deletePingHistoryButton = new Button("Delete Ping History",VaadinIcon.TRASH.create());
        deletePingHistoryButton.addClassName("header-button");
        deletePingHistoryButton.getStyle().set("background-color", "red");
        deletePingHistoryButton.setWidthFull();

        deletePingHistoryButton.addClickListener(e -> {
            Timestamp startDate = Timestamp.valueOf(historyFrom.getValue().atStartOfDay());
            Timestamp endDate = Timestamp.valueOf(historyTo.getValue().atStartOfDay());
            int result = databaseDataEngine.removePingDataBetweenDates(startDate, endDate);
            if(result > 0){
                Notification.show("Ping history deleted successfully " + result + " rows");
            }else{
                Notification.show("Error deleting ping history");
            }
        });


        mainLayout.add(allowPingHistoryDeletionCheckbox);

        HorizontalLayout historyLayout = new HorizontalLayout();
        historyLayout.setAlignItems(Alignment.CENTER);
        historyLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        historyLayout.setPadding(true);
        historyLayout.setSpacing(true);
        historyLayout.setWidthFull();

        historyLayout.add(historyFrom,historyTo);
        mainLayout.add(historyLayout);
        mainLayout.add(deletePingHistoryButton);
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
