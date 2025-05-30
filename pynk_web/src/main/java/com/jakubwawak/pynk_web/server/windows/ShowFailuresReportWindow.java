/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.windows;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.PingData;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class ShowFailuresReportWindow extends Dialog {

    private String width = "80%";
    private String height = "80%";

    VerticalLayout mainLayout;

    ArrayList<PingData> failures;
    Grid<PingData> failuresGrid;

    Button exportToCSVButton;

    /**
     * Constructor
     */
    public ShowFailuresReportWindow() {

        setHeaderTitle("Failures report - 24 hours");
        setWidth(width);
        setHeight(height);

        mainLayout = new VerticalLayout();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        mainLayout.setSizeFull();

        prepareFailuresGrid();

        mainLayout.add(failuresGrid);
        mainLayout.add(exportToCSVButton);

        add(mainLayout);
    }

    /**
     * Prepare the failures grid
     */
    private void prepareFailuresGrid() {
        DatabaseDataEngine databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        failures = databaseDataEngine.getFailuresFrom24h();
        failuresGrid = new Grid<>(PingData.class, false);
        failuresGrid.setItems(failures);

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

}
