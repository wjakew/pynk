/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.grid.Grid;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.checkerframework.checker.units.qual.kN;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.entity.PingData;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/**
 * DataViewer Component
 */
public class DataViewerComponent extends VerticalLayout{

    ArrayList<PingData> content;
    Grid<PingData> pingDataGrid;

    DatabaseDataEngine databaseDataEngine;

    HorizontalLayout headerLayout;

    DateTimePicker startDatePicker;
    DateTimePicker endDatePicker;

    /**
     * Constructor
     */
    public DataViewerComponent(){
        databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.databaseEngine);
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareContent();
    }

    /**
     * Prepare content
     */
    private void prepareContent(){
        startDatePicker = new DateTimePicker();
        startDatePicker.setLabel("Start Date");
        startDatePicker.setValue(LocalDateTime.now().minusHours(1));

        endDatePicker = new DateTimePicker();
        endDatePicker.setLabel("End Date");
        endDatePicker.setValue(LocalDateTime.now());

        content = databaseDataEngine.getPingDataBetweenDates(Timestamp.valueOf(startDatePicker.getValue()), Timestamp.valueOf(endDatePicker.getValue()));
        
        pingDataGrid = new Grid<>(PingData.class,false);
        
        pingDataGrid.addColumn(PingData::getPingTimestamp).setHeader("Timestamp");
        pingDataGrid.addColumn(PingData::getHostName).setHeader("Host Name");

        pingDataGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
            Span pending = new Span(pingData.packetStatusCode);
            if ( pingData.packetStatusCode.equals("Success") ){
                pending.getElement().getThemeList().add("badge success");
            } else if ( pingData.packetStatusCode.equals("Partial loss") ){
                pending.getElement().getThemeList().add("badge contrast");
            } else {
                pending.getElement().getThemeList().add("badge error");
            }
            HorizontalLayout horizontalLayout = new HorizontalLayout(pending);
            return horizontalLayout;
        })).setHeader("Status");
        
        pingDataGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {

            double avg = pingData.getTimeAvg();
            double avgLastDay = databaseDataEngine.getAverageAveragePingTimeFromLastDay(pingData.hostId);

            Span max = new Span(String.valueOf(avg));
            if ( Math.abs(avg - avgLastDay) <= 5 && avg != -1){
                max.getElement().getThemeList().add("badge success");
            } else if ( avg > avgLastDay ){
                max.getElement().getThemeList().add("badge error");
            } else {
                max.getElement().getThemeList().add("badge contrast");
            }
            return max;
        })).setHeader("Avg (ms)");

        pingDataGrid.addColumn(new ComponentRenderer<Component, PingData>(pingData -> {
            H6 dailyAvg = new H6(String.valueOf(databaseDataEngine.getAverageAveragePingTimeFromLastDay(pingData.hostId)));
            return dailyAvg;
        })).setHeader("Last 24h avg (ms)");
        
        add(pingDataGrid);

        pingDataGrid.setItems(content);
        pingDataGrid.setSizeFull();
    }

    /**
     * Prepare layout
     */
    void prepareLayout(){
        headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.START);
        headerLayout.setAlignItems(Alignment.CENTER);

        FlexLayout leftLayout,rightLayout;

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

        leftLayout.add(logo);
        rightLayout.add(startDatePicker,endDatePicker);

        headerLayout.add(leftLayout,rightLayout);

        add(headerLayout,pingDataGrid);
    }
}
