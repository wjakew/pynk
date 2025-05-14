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
import com.vaadin.flow.component.grid.dataview.GridListDataView;

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
import com.vaadin.flow.component.combobox.ComboBox;
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

    public Button settingsButton;
    public Popover settingsPopover;

    public GridListDataView<PingData> dataView;


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

        DataViewerSettingsComponent settingsComponent = new DataViewerSettingsComponent(this);

        settingsButton = new Button("Filtering", VaadinIcon.COG.create());
        settingsButton.addClassName("header-button");


        content = databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(settingsComponent.startDatePicker.getValue()),
                Timestamp.valueOf(settingsComponent.endDatePicker.getValue()));

        pingDataGrid = new Grid<>(PingData.class, false);

        settingsComponent.startDatePicker.addValueChangeListener(event -> {
            content.clear();
            content.addAll(databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(settingsComponent.startDatePicker.getValue()),
                    Timestamp.valueOf(settingsComponent.endDatePicker.getValue())));
            pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed - start date changed");
        });

        settingsComponent.endDatePicker.addValueChangeListener(event -> {
            content.clear();
            content.addAll(databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(settingsComponent.startDatePicker.getValue()),
                    Timestamp.valueOf(settingsComponent.endDatePicker.getValue())));
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

        dataView = pingDataGrid.setItems(content);

        settingsPopover = new Popover(settingsComponent);
        settingsPopover.setTarget(settingsButton);
        settingsPopover.setHeight("600px");
        settingsPopover.setWidth("550px");

        dataView.addFilter(pingData -> {
            if (settingsComponent.pingPacketStatusComboBox.getValue().equals("All")) {
                return true;
            }
            return pingData.packetStatusCode.equals(settingsComponent.pingPacketStatusComboBox.getValue());
        });

        settingsComponent.pingPacketStatusComboBox.addValueChangeListener(e -> {
            dataView.refreshAll();
            Notification.show("Data refreshed - packet status changed");
        });

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
        logo.getStyle().set("margin-left", "10px");

        H5 timeRangeLabelFrom = new H5("from:");
        timeRangeLabelFrom.addClassName("logo");
        timeRangeLabelFrom.getStyle().set("margin-right", "5px");
        timeRangeLabelFrom.getStyle().set("font-size", "1.5rem");

        H5 timeRangeLabelTo = new H5("to:");
        timeRangeLabelTo.addClassName("logo");
        timeRangeLabelTo.getStyle().set("margin-right", "10px");
        timeRangeLabelTo.getStyle().set("font-size", "1.5rem");

        leftLayout.add(logo, logo);
        rightLayout.add(settingsButton);

        headerLayout.add(leftLayout, rightLayout);

        add(headerLayout, pingDataGrid);
    }
}

/**
 * DataViewerSettingsComponent - settings for DataViewerComponent
 */
class DataViewerSettingsComponent extends VerticalLayout {

    Button refreshButton;
    Button showLastHourButton;
    Button clearTimeRangeButton;

    ComboBox<String> pingPacketStatusComboBox;

    DateTimePicker startDatePicker;
    DateTimePicker endDatePicker;

    Button exportToCSVButton;

    DataViewerComponent parent;

    /**
     * Constructor
     * 
     * @param parent
     */
    DataViewerSettingsComponent(DataViewerComponent parent) {
        this.parent = parent;

        pingPacketStatusComboBox = new ComboBox<>("Packet Status");
        pingPacketStatusComboBox.setLabel("");
        pingPacketStatusComboBox.setItems("Success", "Partial loss", "No response", "All");
        pingPacketStatusComboBox.setValue("All");
        pingPacketStatusComboBox.setWidth("100%");

        startDatePicker = new DateTimePicker("");
        startDatePicker.setLabel("");
        startDatePicker.setValue(LocalDateTime.now().minusHours(1));
        startDatePicker.setWidth("100%");

        startDatePicker.getStyle().set("margin-right", "10px");

        endDatePicker = new DateTimePicker("");
        endDatePicker.setLabel("");
        endDatePicker.setValue(LocalDateTime.now());
        endDatePicker.setWidth("100%");

        endDatePicker.getStyle().set("margin-left", "10px");

        refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addClassName("header-button");
        refreshButton.setWidth("100%");

        refreshButton.addClickListener(event -> {
            parent.content.clear();
            parent.content.addAll(parent.databaseDataEngine.getPingDataBetweenDates(
                    Timestamp.valueOf(startDatePicker.getValue()),
                    Timestamp.valueOf(endDatePicker.getValue())));
            parent.pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed");
        });

        showLastHourButton = new Button("Show last hour", VaadinIcon.CLOCK.create());
        showLastHourButton.addClassName("header-button");
        showLastHourButton.setWidth("100%");

        showLastHourButton.addClickListener(event -> {
            startDatePicker.setValue(LocalDateTime.now().minusHours(1));
            endDatePicker.setValue(LocalDateTime.now());
            parent.content.clear();
            parent.content.addAll(parent.databaseDataEngine.getPingDataBetweenDates(
                    Timestamp.valueOf(startDatePicker.getValue()),
                    Timestamp.valueOf(endDatePicker.getValue())));
            parent.pingDataGrid.getDataProvider().refreshAll();
            Notification.show("Data refreshed - last hour shown");
        });


        clearTimeRangeButton = new Button("Clear time range", VaadinIcon.ERASER.create());
        clearTimeRangeButton.addClassName("header-button");
        clearTimeRangeButton.setWidth("100%");
        clearTimeRangeButton.addClickListener(event -> {
            startDatePicker.setValue(LocalDateTime.now().minusHours(1));
            endDatePicker.setValue(LocalDateTime.now());
            parent.content.clear();
            parent.content.addAll(parent.databaseDataEngine.getPingDataBetweenDates(
                    Timestamp.valueOf(startDatePicker.getValue()),
                    Timestamp.valueOf(endDatePicker.getValue())));
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

        add(new H6("Settings"), pingPacketStatusComboBox, new H6("Time range from"), startDatePicker, new H6("Time range to"), endDatePicker, refreshButton, showLastHourButton, clearTimeRangeButton,exportToCSVButton);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

}
