/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;
import com.jakubwawak.pynk_web.entity.Host;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;

/**
 * Wrapper for HostAvgPingChartComponent
 */
public class HostAvgPingChartWrapper extends VerticalLayout {

    ComboBox<Host> hostComboBox;
    DateTimePicker startDatePicker;
    DateTimePicker endDatePicker;

    DatabaseEngine databaseEngine;

    Host host;
    Timestamp startDate;
    Timestamp endDate;

    HostAvgPingChartComponent chart;

    public HostAvgPingChartWrapper(Host host, Timestamp startDate, Timestamp endDate) {
        addClassName("host-chart-wrapper");
        this.host = host;
        this.startDate = startDate;
        this.endDate = endDate;

        databaseEngine = PynkWebApplication.databaseEngine;

        hostComboBox = new ComboBox<>();
        hostComboBox.setLabel("Host");
        hostComboBox.setItems(databaseEngine.getHosts());
        hostComboBox.setItemLabelGenerator(Host::getHostName);
        hostComboBox.setWidthFull();

        startDatePicker = new DateTimePicker();
        startDatePicker.setLabel("Start Date");
        startDatePicker.getStyle().set("margin-right", "10px");

        endDatePicker = new DateTimePicker();
        endDatePicker.setLabel("End Date");
        endDatePicker.getStyle().set("margin-left", "10px");

        if (host == null && startDate == null && endDate == null) {
            hostComboBox.setValue(databaseEngine.getHosts().get(0));
            LocalDateTime now = LocalDateTime.now(ZoneId.of("Europe/Warsaw"));
            String nowString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime start = now.minusHours(1);
            String startString = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime end = now;
            String endString = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            startDatePicker.setValue(start);
            endDatePicker.setValue(end);
        } else {
            hostComboBox.setValue(host);
            startDatePicker.setValue(startDate.toLocalDateTime());
            endDatePicker.setValue(endDate.toLocalDateTime());
        }

        hostComboBox.addValueChangeListener(e -> {
            updateChart();
            Notification.show("Host changed to " + hostComboBox.getValue().getHostName());
        });

        startDatePicker.addValueChangeListener(e -> {
            updateChart();
            Notification.show("Start date changed to " + startDatePicker.getValue());
        });

        endDatePicker.addValueChangeListener(e -> {
            updateChart();
            Notification.show("End date changed to " + endDatePicker.getValue());
        });

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareLayout();
    }

    /**
     * Prepares the layout
     */
    void prepareLayout() {
        removeAll();
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.CENTER);

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

        leftLayout.add(hostComboBox);
        rightLayout.add(startDatePicker, endDatePicker);

        header.add(leftLayout, rightLayout);

        add(header);
        updateChart();
    }

    /**
     * Updates the chart
     */
    private void updateChart() {
        if (chart != null) {
            remove(chart);
        }
        chart = new HostAvgPingChartComponent(hostComboBox.getValue(),
                new Timestamp(startDatePicker.getValue().atZone(ZoneId.of("Europe/Warsaw")).toInstant().toEpochMilli()),
                new Timestamp(endDatePicker.getValue().atZone(ZoneId.of("Europe/Warsaw")).toInstant().toEpochMilli()));
        add(chart);
    }

}