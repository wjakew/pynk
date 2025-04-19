/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.popover.Popover;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.PingData;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.jakubwawak.pynk_web.server.windows.FileDownloaderWindow;
import java.io.File;

/**
 * DataViewer Component
 */
public class DataViewerComponent extends VerticalLayout {

    public ArrayList<PingData> content;
    public Grid<PingData> pingDataGrid;

    public DatabaseDataEngine databaseDataEngine;

    public HorizontalLayout headerLayout;

    public DateTimePicker startDatePicker;
    public DateTimePicker endDatePicker;

    public Button settingsButton;
    public Popover settingsPopover;

    /**
     * Constructor
     */
    public DataViewerComponent() {
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareContent();
        prepareLayout();
    }

    /**
     * Prepare content
     */
    private void prepareContent() {

        settingsButton = new Button("", VaadinIcon.COG.create());
        settingsButton.addClassName("header-button");

        startDatePicker = new DateTimePicker();
        startDatePicker.setLabel("");
        startDatePicker.setValue(LocalDateTime.now().minusHours(1));
        startDatePicker.setVisible(false);

        startDatePicker.getStyle().set("margin-right", "10px");

        endDatePicker = new DateTimePicker();
        endDatePicker.setLabel("");
        endDatePicker.setValue(LocalDateTime.now());
        endDatePicker.setVisible(false);

        endDatePicker.getStyle().set("margin-left", "10px");

        content = databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(startDatePicker.getValue()),
                Timestamp.valueOf(endDatePicker.getValue()));

        pingDataGrid = new Grid<>(PingData.class, false);

        startDatePicker.addValueChangeListener(event -> {
            content.clear();
            content.addAll(databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(startDatePicker.getValue()),
                    Timestamp.valueOf(endDatePicker.getValue())));
            pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed - start date changed");
        });

        endDatePicker.addValueChangeListener(event -> {
            content.clear();
            content.addAll(databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(startDatePicker.getValue()),
                    Timestamp.valueOf(endDatePicker.getValue())));
            pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed - end date changed");
        });

        pingDataGrid.addColumn(PingData::getPingTimestamp).setHeader("Timestamp").setResizable(true);
        pingDataGrid.addColumn(PingData::getHostName).setHeader("Host Name").setResizable(true);

        pingDataGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
            Span pending = new Span(pingData.packetStatusCode);
            if (pingData.packetStatusCode.equals("Success")) {
                pending.getElement().getThemeList().add("badge success");
            } else if (pingData.packetStatusCode.equals("Partial loss")) {
                pending.getElement().getThemeList().add("badge contrast");
            } else {
                pending.getElement().getThemeList().add("badge error");
            }
            HorizontalLayout horizontalLayout = new HorizontalLayout(pending);
            return horizontalLayout;
        })).setHeader("Status");

        pingDataGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {

            double avg = pingData.getTimeAvg();
            double avgLastDay = databaseDataEngine.getAverageAveragePingTimeFromLastDay(pingData.hostIdMongo);

            Span max = new Span(String.valueOf(avg));
            if (Math.abs(avg - avgLastDay) <= 5 && avg != -1) {
                max.getElement().getThemeList().add("badge success");
            } else if (avg > avgLastDay) {
                max.getElement().getThemeList().add("badge error");
            } else {
                max.getElement().getThemeList().add("badge contrast");
            }
            return max;
        })).setHeader("Avg (ms)");

        pingDataGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
            H6 dailyAvg = new H6(
                    String.valueOf(databaseDataEngine.getAverageAveragePingTimeFromLastDay(pingData.hostIdMongo)));
            return dailyAvg;
        })).setHeader("Last 24h avg (ms)");

        pingDataGrid.setItems(content);
        pingDataGrid.setSizeFull();

        settingsPopover = new Popover(new DataViewerSettingsComponent(this));
        settingsPopover.setTarget(settingsButton);
        settingsPopover.setHeight("700px");
        settingsPopover.setWidth("300px");

    }

    /**
     * Prepare layout
     */
    void prepareLayout() {
        headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setJustifyContentMode(JustifyContentMode.START);
        headerLayout.setAlignItems(Alignment.CENTER);

        FlexLayout leftLayout, rightLayout;

        leftLayout = new FlexLayout();
        leftLayout.setWidthFull();
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setAlignItems(Alignment.CENTER);

        rightLayout = new FlexLayout();
        rightLayout.setWidthFull();
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);

        H4 logo = new H4("data");
        logo.addClassName("logo");
        logo.getStyle().set("margin-left", "10px");

        H5 timeRangeLabelFrom = new H5("from:");
        timeRangeLabelFrom.addClassName("logo");
        timeRangeLabelFrom.getStyle().set("margin-right", "5px");
        timeRangeLabelFrom.getStyle().set("font-size", "1.5rem");

        H5 timeRangeLabelTo = new H5("to:");
        timeRangeLabelTo.addClassName("logo");
        timeRangeLabelTo.getStyle().set("margin-right", "5px");
        timeRangeLabelTo.getStyle().set("font-size", "1.5rem");

        leftLayout.add(logo, logo);
        rightLayout.add(timeRangeLabelFrom, startDatePicker, timeRangeLabelTo, endDatePicker, settingsButton);

        headerLayout.add(leftLayout, rightLayout);

        add(headerLayout, pingDataGrid);
    }
}

class DataViewerSettingsComponent extends VerticalLayout {

    Button refreshButton;
    Button showLastHourButton;
    Button visibleTimeRangeButton;
    Button clearTimeRangeButton;

    Button exportToCSVButton;

    DataViewerComponent parent;

    /**
     * Constructor
     * 
     * @param parent
     */
    DataViewerSettingsComponent(DataViewerComponent parent) {
        this.parent = parent;

        refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addClassName("header-button");
        refreshButton.setWidth("100%");

        refreshButton.addClickListener(event -> {
            parent.content.clear();
            parent.content.addAll(parent.databaseDataEngine.getPingDataBetweenDates(
                    Timestamp.valueOf(parent.startDatePicker.getValue()),
                    Timestamp.valueOf(parent.endDatePicker.getValue())));
            parent.pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed");
        });

        showLastHourButton = new Button("Show last hour", VaadinIcon.CLOCK.create());
        showLastHourButton.addClassName("header-button");
        showLastHourButton.setWidth("100%");

        showLastHourButton.addClickListener(event -> {
            parent.startDatePicker.setValue(LocalDateTime.now().minusHours(1));
            parent.endDatePicker.setValue(LocalDateTime.now());
            parent.content.clear();
            parent.content.addAll(parent.databaseDataEngine.getPingDataBetweenDates(
                    Timestamp.valueOf(parent.startDatePicker.getValue()),
                    Timestamp.valueOf(parent.endDatePicker.getValue())));
            parent.pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed - last hour shown");
        });

        visibleTimeRangeButton = new Button("Show Time Query", VaadinIcon.CALENDAR.create());
        visibleTimeRangeButton.addClassName("header-button");
        visibleTimeRangeButton.setWidth("100%");

        visibleTimeRangeButton.addClickListener(event -> {
            if (parent.startDatePicker.isVisible()) {
                parent.startDatePicker.setVisible(false);
                parent.endDatePicker.setVisible(false);
            } else {
                parent.startDatePicker.setVisible(true);
                parent.endDatePicker.setVisible(true);
            }
        });

        clearTimeRangeButton = new Button("Clear time range", VaadinIcon.ERASER.create());
        clearTimeRangeButton.addClassName("header-button");
        clearTimeRangeButton.setWidth("100%");
        clearTimeRangeButton.addClickListener(event -> {
            parent.startDatePicker.setValue(LocalDateTime.now().minusHours(1));
            parent.endDatePicker.setValue(LocalDateTime.now());
            parent.content.clear();
            parent.content.addAll(parent.databaseDataEngine.getPingDataBetweenDates(
                    Timestamp.valueOf(parent.startDatePicker.getValue()),
                    Timestamp.valueOf(parent.endDatePicker.getValue())));
            parent.pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed - time range cleared");
        });

        exportToCSVButton = new Button("Export to CSV", VaadinIcon.FILE.create());
        exportToCSVButton.addClassName("header-button");
        exportToCSVButton.setWidth("100%");

        exportToCSVButton.addClickListener(event -> {
            StringBuilder csvData = new StringBuilder();
            csvData.append("Timestamp,Host Name,Status,Average Time\n"); // Header

            for (PingData pingData : parent.content) {
                csvData.append(pingData.getPingTimestamp()).append(",")
                        .append(pingData.getHostName()).append(",")
                        .append(pingData.packetStatusCode).append(",")
                        .append(pingData.getTimeAvg()).append("\n");
            }

            // Create a CSV file and write the data
            String fileName = "ping_data_export.csv";
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                fileWriter.write(csvData.toString());
                Notification.show("Data exported to " + fileName);
                String absolutePath = Paths.get(fileName).toAbsolutePath().toString();
                File file = new File(absolutePath);
                if (file.exists()) {
                    FileDownloaderWindow fileDownloaderWindow = new FileDownloaderWindow(file);
                    add(fileDownloaderWindow.dialog);
                    fileDownloaderWindow.dialog.open();
                }
            } catch (IOException e) {
                Notification.show("Error exporting data: " + e.getMessage());
            }
        });

        add(new H6("Settings"), refreshButton, showLastHourButton, visibleTimeRangeButton, clearTimeRangeButton,
                exportToCSVButton);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

}
