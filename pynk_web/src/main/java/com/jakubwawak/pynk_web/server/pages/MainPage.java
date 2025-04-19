/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.pages;

import com.jakubwawak.pynk_web.server.components.DashboardComponent;
import com.jakubwawak.pynk_web.server.components.DataViewerComponent;
import com.jakubwawak.pynk_web.server.components.HeaderComponent;
import com.jakubwawak.pynk_web.server.components.HostManagementComponent;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

/**
 * Main Application Page
 */
@Route(value = "/home")
@PageTitle("Pynk Web")
@RouteAlias(value = "/")
public class MainPage extends VerticalLayout {

    HeaderComponent header;

    public HostManagementComponent hostManagementComponent;
    public DataViewerComponent dataViewerComponent;
    public DashboardComponent dashboardComponent;

    /**
     * Constructor
     */
    public MainPage() {
        addClassName("page");
        prepareHeader();
        prepareContent();
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    /**
     * Prepare header
     */
    void prepareHeader() {
        header = new HeaderComponent(this);
        add(header);
    }

    /**
     * Prepare content
     */
    void prepareContent() {
        hostManagementComponent = new HostManagementComponent();
        dataViewerComponent = new DataViewerComponent();
        dashboardComponent = new DashboardComponent();

        add(hostManagementComponent, dataViewerComponent, dashboardComponent);

        hostManagementComponent.setVisible(false);
        dataViewerComponent.setVisible(false);
        dashboardComponent.setVisible(true);
    }

}
