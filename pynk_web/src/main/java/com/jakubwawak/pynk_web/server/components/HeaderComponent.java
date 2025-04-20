/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import com.jakubwawak.pynk_web.server.pages.MainPage;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;

/**
 * Header Component
 */
public class HeaderComponent extends HorizontalLayout {

    FlexLayout leftLayout, rightLayout;

    Button dashboardButton, manageHostsButton, dataButton;

    H4 logo;

    MainPage parent;

    /**
     * Constructor
     * 
     * @param parent
     */
    public HeaderComponent(MainPage parent) {
        this.parent = parent;
        addClassName("header");
        setWidthFull();

        prepareHeader();

        setJustifyContentMode(JustifyContentMode.BETWEEN);
        setVerticalComponentAlignment(Alignment.CENTER);
        setAlignItems(Alignment.CENTER);
    }

    /**
     * Prepare the header
     */
    void prepareHeader() {

        manageHostsButton = new Button("Manage Hosts", VaadinIcon.GLOBE.create());
        manageHostsButton.addClassName("header-button");

        manageHostsButton.addClickListener(event -> {
            parent.hostManagementComponent.setVisible(!parent.hostManagementComponent.isVisible());
            parent.dataViewerComponent.setVisible(false);
            parent.dashboardComponent.setVisible(false);
            parent.hostManagementComponent.refreshContent();
        });

        dataButton = new Button("Data", VaadinIcon.DATABASE.create());
        dataButton.addClassName("header-button");
        dataButton.getStyle().set("margin-right", "10px");
        StreamResource res = new StreamResource("pynk-logo.png", () -> {
            return MainPage.class.getClassLoader().getResourceAsStream("images/pynk_icon.png");
        });

        dataButton.addClickListener(event -> {
            parent.dataViewerComponent.setVisible(!parent.dataViewerComponent.isVisible());
            parent.hostManagementComponent.setVisible(false);
            parent.dashboardComponent.setVisible(false);
            // parent.dataViewerComponent.refreshContent();
        });

        dashboardButton = new Button("Dashboard", VaadinIcon.HOME.create());
        dashboardButton.addClassName("header-button");
        dashboardButton.getStyle().set("margin-right", "10px");

        dashboardButton.addClickListener(event -> {
            parent.dashboardComponent.setVisible(!parent.dashboardComponent.isVisible());
            parent.hostManagementComponent.setVisible(false);
            parent.dataViewerComponent.setVisible(false);
        });

        Image logoImage = new Image(res, "pynk-logo");
        logoImage.setWidth("50px");
        logoImage.setHeight("50px");

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

        logo = new H4("pynk");
        logo.addClassName("logo");
        logo.getStyle().set("margin-left", "10px");

        leftLayout.add(logoImage, logo);

        rightLayout.add(dashboardButton, dataButton, manageHostsButton);

        add(leftLayout, rightLayout);
    }
}
