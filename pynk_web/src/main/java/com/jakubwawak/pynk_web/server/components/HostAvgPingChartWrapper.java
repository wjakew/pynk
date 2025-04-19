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

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;
import com.jakubwawak.pynk_web.entity.Host;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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

        startDatePicker = new DateTimePicker();
        startDatePicker.setLabel("Start Date");

        endDatePicker = new DateTimePicker();
        endDatePicker.setLabel("End Date");

        if (host == null && startDate == null && endDate == null) {
            hostComboBox.setValue(databaseEngine.getHosts().get(0));
            startDatePicker.setValue(LocalDateTime.now().minusHours(1));
            endDatePicker.setValue(LocalDateTime.now());
        } else {
            hostComboBox.setValue(host);
            startDatePicker.setValue(startDate.toLocalDateTime());
            endDatePicker.setValue(endDate.toLocalDateTime());
        }

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

        FlexLayout leftLayout = new FlexLayout();
        leftLayout.setSizeFull();
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setAlignItems(Alignment.CENTER);
        leftLayout.setWidth("20%");

        FlexLayout rightLayout = new FlexLayout();
        rightLayout.setSizeFull();
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);
        rightLayout.setWidth("80%");

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
        HostAvgPingChartComponent chart = new HostAvgPingChartComponent(hostComboBox.getValue(),
                new Timestamp(startDatePicker.getValue().toEpochSecond(ZoneOffset.UTC) * 1000),
                new Timestamp(endDatePicker.getValue().toEpochSecond(ZoneOffset.UTC) * 1000));
        add(chart);
    }

}