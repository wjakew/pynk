/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseDataEngine;
import com.jakubwawak.pynk_web.server.windows.ShowFailuresReportWindow;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;

/**
 * DashboardComponent
 */
public class DashboardComponent extends VerticalLayout {

    HostAvgPingChartWrapper hostAvgPingChartWrapper1;
    StatisticsComponent statisticsComponent;

    HorizontalLayout headerLayout;

    HorizontalLayout lastStatisticsLayout;

    Button getFailuresButton;

    TabSheet viewSheets;

    /**
     * Constructor
     */
    public DashboardComponent() {
        addClassName("dashboard-component");

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        prepareLayout();
    }

    /**
     * Prepares the last statistics layout
     */
    private void prepareLastStatisticsLayout() {
        DatabaseDataEngine databaseDataEngine = new DatabaseDataEngine(PynkWebApplication.getDatabaseEngine());
        int numberOfSuccessesFrom24h = databaseDataEngine.getNumberOfSuccessesFrom24h();
        int numberOfFailuresFrom24h = databaseDataEngine.getNumberOfFailuresFrom24h();
        lastStatisticsLayout = new HorizontalLayout();
        lastStatisticsLayout.setWidth("100%");
        lastStatisticsLayout.setJustifyContentMode(JustifyContentMode.END);
        lastStatisticsLayout.setAlignItems(Alignment.END);

        Span successes = new Span("Ping OK: " + String.valueOf(numberOfSuccessesFrom24h));
        successes.getElement().getThemeList().add("badge success");
        successes.getStyle().set("margin-right", "10px");

        Span failures = new Span("Ping Failed: " + String.valueOf(numberOfFailuresFrom24h));
        failures.getElement().getThemeList().add("badge error");

        Icon successIcon = VaadinIcon.CHART_LINE.create();
        successIcon.getStyle().set("margin-right", "5px");
        successIcon.getStyle().set("color", "pink");

        getFailuresButton = new Button("", VaadinIcon.RECORDS.create());
        getFailuresButton.addClassName("header-button");
        getFailuresButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        getFailuresButton.getStyle().set("margin-left", "10px");

        getFailuresButton.addClickListener(event -> {
            ShowFailuresReportWindow showFailuresReportWindow = new ShowFailuresReportWindow();
            add(showFailuresReportWindow);
            showFailuresReportWindow.open();
        });

        lastStatisticsLayout.add(successIcon, successes, failures);
    }

    /**
     * Prepares the layout
     */
    private void prepareLayout() {

        viewSheets = new TabSheet();
        viewSheets.setSizeFull();

        headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.setJustifyContentMode(JustifyContentMode.CENTER);
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

        H4 logo = new H4("dashboard");
        logo.addClassName("logo");
        logo.getStyle().set("margin-left", "10px");

        leftLayout.add(logo);

        prepareLastStatisticsLayout();
        rightLayout.add(lastStatisticsLayout, getFailuresButton);

        hostAvgPingChartWrapper1 = new HostAvgPingChartWrapper(null, null, null);
        statisticsComponent = new StatisticsComponent();

        headerLayout.add(leftLayout, rightLayout);
        add(headerLayout);

        viewSheets.add("Statistics", statisticsComponent);
        viewSheets.add("Host Ping History", hostAvgPingChartWrapper1);

        viewSheets.addSelectedChangeListener(event->{
            statisticsComponent.prepareLayout();
        });
        add(viewSheets);
    }

}
