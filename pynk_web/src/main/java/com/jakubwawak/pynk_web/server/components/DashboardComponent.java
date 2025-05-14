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

/**
 * DashboardComponent
 */
public class DashboardComponent extends VerticalLayout {

    HostAvgPingChartWrapper hostAvgPingChartWrapper1;
    StatisticsComponent statisticsComponent;
    LostPingComponent lostPingComponent;
    HostsDashboardComponent hostsDashboardComponent;

    HorizontalLayout headerLayout;

    HorizontalLayout lastStatisticsLayout;

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
        int numberOfNoResponseFrom24h = databaseDataEngine.getNumberOfNoResponseFrom24h();
        int numberOfPartialLossFrom24h = databaseDataEngine.getNumberOfPartialLossFrom24h();

        lastStatisticsLayout = new HorizontalLayout();
        lastStatisticsLayout.setWidth("100%");
        lastStatisticsLayout.setJustifyContentMode(JustifyContentMode.END);
        lastStatisticsLayout.setAlignItems(Alignment.END);

        Span successes = new Span("Ping OK: " + String.valueOf(numberOfSuccessesFrom24h));
        successes.getElement().getThemeList().add("badge success");
        successes.getStyle().set("margin-right", "10px");

        Span failures = new Span("Ping No Response: " + String.valueOf(numberOfNoResponseFrom24h));
        failures.getElement().getThemeList().add("badge error");

        Span partialLoss = new Span("Ping Partial Loss: " + String.valueOf(numberOfPartialLossFrom24h));
        partialLoss.getElement().getThemeList().add("badge contrast");
        partialLoss.getStyle().set("margin-right", "10px");

        Icon successIcon = VaadinIcon.CHART_LINE.create();
        successIcon.getStyle().set("margin-right", "5px");
        successIcon.getStyle().set("color", "pink");

        lastStatisticsLayout.add(successIcon, successes, partialLoss, failures);
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
        logo.getStyle().set("margin-left", "10px");

        leftLayout.add(logo);

        prepareLastStatisticsLayout();
        rightLayout.add(lastStatisticsLayout);

        hostAvgPingChartWrapper1 = new HostAvgPingChartWrapper(null, null, null);
        statisticsComponent = new StatisticsComponent();
        lostPingComponent = new LostPingComponent();

        hostsDashboardComponent = new HostsDashboardComponent();

        headerLayout.add(leftLayout, rightLayout);
        add(headerLayout);

        viewSheets.add("Statistics", statisticsComponent);
        viewSheets.add("Ping History", hostAvgPingChartWrapper1);
        viewSheets.add("Connection Errors", lostPingComponent);
        viewSheets.add("Host Tree", hostsDashboardComponent);
        viewSheets.addSelectedChangeListener(event -> {
            statisticsComponent.prepareLayout();
            lostPingComponent.refreshFailuresGrid();
        });
        add(viewSheets);
    }

}
