/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.PingData;
import com.jakubwawak.pynk_web.server.windows.FileDownloaderWindow;
import com.jakubwawak.pynk_web.server.windows.PingDataDetailsWindow;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * LostPingComponent
 */
public class LostPingComponent extends VerticalLayout{

    DatabaseDataEngine databaseDataEngine;

    ArrayList<PingData> failures;
    Grid<PingData> failuresGrid;

    HorizontalLayout topLayout;

    Button exportToCSVButton;

    /**
     * Constructor
     */
    public LostPingComponent() {
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        prepareFailuresGrid();
        prepareTopLayout();

        add(topLayout);
        add(failuresGrid);

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    /**
     * Prepare the top layout
     */
    private void prepareTopLayout(){
        topLayout = new HorizontalLayout();
        topLayout.setWidthFull();
        topLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        topLayout.setAlignItems(Alignment.CENTER);

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

        H1 statusHeader = new H1("Lost Pings");
        statusHeader.addClassName("logo");


        leftLayout.add(statusHeader);

        rightLayout.add(exportToCSVButton);
        
        topLayout.add(leftLayout, rightLayout);
    }


    /**
     * Prepare the failures grid
     */
    private void prepareFailuresGrid() {
        DatabaseDataEngine databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        failures = databaseDataEngine.getFailuresFrom24h();
        failuresGrid = new Grid<>(PingData.class, false);
        failuresGrid.setItems(failures);
        failuresGrid.setSizeFull();

        failuresGrid.addColumn(PingData::getPingTimestamp).setHeader("Timestamp").setResizable(true);
        failuresGrid.addColumn(PingData::getHostName).setHeader("Host Name").setResizable(true);

        failuresGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
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

        failuresGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
            Text traceRouteStatus = new Text(pingData.packetTracertData);
            VerticalLayout verticalLayout = new VerticalLayout(traceRouteStatus);
            verticalLayout.setWidth("100%");
            verticalLayout.setHeight("250px");
            verticalLayout.getStyle().set("text-wrap", "wrap");
            return verticalLayout;
        })).setHeader("Trace Route Status").setResizable(true).addClassName("column");

        failuresGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
            Text digData = new Text(pingData.packetDigData);
            VerticalLayout verticalLayout = new VerticalLayout(digData);
            verticalLayout.setWidth("100%");
            verticalLayout.setHeight("250px");
            verticalLayout.getStyle().set("text-wrap", "wrap");
            return verticalLayout;
        })).setHeader("Dig Status").setResizable(true).addClassName("column");

        failuresGrid.setSizeFull();

        failuresGrid.addItemClickListener(item -> {
            PingDataDetailsWindow pingDataDetailsWindow = new PingDataDetailsWindow(item.getItem());
            add(pingDataDetailsWindow);
            pingDataDetailsWindow.open();
        });

        exportToCSVButton = new Button("Export to CSV", VaadinIcon.DOWNLOAD.create());
        exportToCSVButton.addClassName("header-button");
        exportToCSVButton.addClickListener(e -> {
            StringBuilder csvData = new StringBuilder();
            csvData.append("Timestamp,Host Name,Status,Average Time,Trace Route Status,Dig Status\n"); // Header

            for (PingData pingData : failures) {
                csvData.append(pingData.getPingTimestamp()).append(",")
                        .append(pingData.getHostName()).append(",")
                        .append(pingData.packetStatusCode).append(",")
                        .append(pingData.getTimeAvg()).append(",")
                        .append(pingData.packetTracertData).append(",")
                        .append(pingData.packetDigData).append("\n");
            }

            // Create a CSV file and write the data
            String fileName = "ping_data_export_" + System.currentTimeMillis() + ".csv";
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
            } catch (IOException ex) {
                Notification.show("Error exporting data: " + ex.getMessage());
            }
        });
    }

    /**
     * Refresh the failures grid
     */
    public void refreshFailuresGrid(){
        failures.clear();
        failures.addAll(databaseDataEngine.getFailuresFrom24h());
        failuresGrid.getDataProvider().refreshAll();
        Notification.show("Failures grid refreshed!");
    }

}
