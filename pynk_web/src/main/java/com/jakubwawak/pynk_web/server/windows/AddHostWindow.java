/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.windows;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;
import com.jakubwawak.pynk_web.entity.Host;
import com.jakubwawak.pynk_web.server.components.HostManagementComponent;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;

/**
 * Add host window
 */
public class AddHostWindow extends Dialog {

    private String width = "80%";
    private String height = "80%";

    VerticalLayout mainLayout;

    private Host host;

    TextField hostName;
    TextField hostIP;

    ComboBox<String> hostType;
    ComboBox<String> hostStatus;

    IntegerField hostJobTime;

    Button saveButton;

    boolean isEdit = false;

    DatabaseEngine databaseEngine;
    HostManagementComponent hostManager;

    /**
     * Constructor
     */
    public AddHostWindow(Host host, HostManagementComponent hostManager) {
        this.host = host;
        this.hostManager = hostManager;
        databaseEngine = PynkWebApplication.databaseEngine;
        setHeaderTitle("Add host");
        setWidth(width);
        setHeight(height);

        mainLayout = new VerticalLayout();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        mainLayout.setSizeFull();

        if (host != null) {
            setHeaderTitle("Edit host");
            isEdit = true;
        } else {
            this.host = new Host();
            isEdit = false;
        }

        prepareLayout();

        add(mainLayout);

    }

    private void prepareLayout() {
        hostName = new TextField("Host name");
        hostName.setPrefixComponent(VaadinIcon.PENCIL.create());
        hostName.setWidthFull();
        hostName.setMaxLength(200);
        hostName.setMinLength(1);
        hostName.setRequired(true);
        hostName.setErrorMessage("Host name is required");
        hostName.setRequiredIndicatorVisible(true);
        hostName.setHelperText("Enter host name");

        hostIP = new TextField("Host IP");
        hostIP.setPrefixComponent(VaadinIcon.GLOBE.create());
        hostIP.setWidthFull();
        hostIP.setMaxLength(100);
        hostIP.setMinLength(1);
        hostIP.setRequired(true);
        hostIP.setErrorMessage("Host IP is required");
        hostIP.setRequiredIndicatorVisible(true);
        hostIP.setHelperText("Enter host IP");

        ArrayList<String> hostTypes = new ArrayList<>();
        hostTypes.add("public");
        hostTypes.add("local");
        hostTypes.add("private");
        hostTypes.add("other");

        hostType = new ComboBox<>("Host type");
        hostType.setWidthFull();
        hostType.setItems(hostTypes);
        hostType.setValue("public");
        hostType.setItemLabelGenerator(hostType -> hostType);
        hostType.setRequired(true);
        hostType.setErrorMessage("Host type is required");
        hostType.setRequiredIndicatorVisible(true);
        hostType.setHelperText("Select host type");

        ArrayList<String> hostStatuses = new ArrayList<>();
        hostStatuses.add("active");
        hostStatuses.add("inactive");

        hostStatus = new ComboBox<>("Host status");
        hostStatus.setWidthFull();
        hostStatus.setItems(hostStatuses);
        hostStatus.setValue("active");
        hostStatus.setItemLabelGenerator(hostStatus -> hostStatus);
        hostStatus.setRequired(true);
        hostStatus.setErrorMessage("Host status is required");
        hostStatus.setRequiredIndicatorVisible(true);
        hostStatus.setHelperText("Select host status");

        hostJobTime = new IntegerField("Time to Update (miliseconds)");
        hostJobTime.setWidthFull();
        hostJobTime.setStepButtonsVisible(true);
        hostJobTime.setMin(0);
        hostJobTime.setMax(100000);
        hostJobTime.setValue(30000);
        hostJobTime.setErrorMessage("Time to Update is required");
        hostJobTime.setRequiredIndicatorVisible(true);
        hostJobTime.setHelperText("Enter time to Update");
        mainLayout.add(hostName);

        HorizontalLayout hostIPLayout = new HorizontalLayout();
        hostIPLayout.setWidthFull();
        hostIPLayout.setAlignItems(Alignment.CENTER);
        hostIPLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        hostIPLayout.add(hostStatus);
        hostIPLayout.add(hostType);

        mainLayout.add(hostIPLayout);

        mainLayout.add(hostIP);

        mainLayout.add(hostJobTime);

        saveButton = new Button("Save", VaadinIcon.CHECK.create(), this::saveHost);
        saveButton.addClassName("header-button");
        saveButton.setWidthFull();
        saveButton.setIcon(VaadinIcon.CHECK.create());

        if (isEdit) {
            hostName.setValue(host.getHostName());
            hostIP.setValue(host.getHostIp());
            hostType.setValue(host.getHostCategory());
            hostStatus.setValue(host.getHostStatus());
            hostJobTime.setValue(host.getHostJobTime());
            saveButton.setText("Update");
        }

        mainLayout.add(saveButton);
    }

    /**
     * Validate fields
     * 
     * @return true if fields are valid, false otherwise
     */
    private boolean validateFields() {
        return !hostName.getValue().isEmpty() && !hostIP.getValue().isEmpty()
                && !hostType.getValue().isEmpty() && !hostStatus.getValue().isEmpty() && hostJobTime.getValue() != null;
    }

    /**
     * Save host
     */
    private void saveHost(ClickEvent<Button> event) {
        if (validateFields()) {
            host.setHostName(hostName.getValue());
            host.setHostIp(hostIP.getValue());
            host.setHostCategory(hostType.getValue());
            host.setHostStatus(hostStatus.getValue());
            host.setHostJobTime(hostJobTime.getValue());

            if (isEdit) {
                int ans = databaseEngine.updateHost(host);
                if (ans > 0) {
                    Notification.show("Host updated successfully", 3000, Notification.Position.BOTTOM_CENTER);
                    hostManager.refreshContent();
                    close();
                } else {
                    Notification.show("Failed to update host", 3000, Notification.Position.BOTTOM_CENTER);
                }
            } else {
                int ans = databaseEngine.addHost(host);
                if (ans > 0) {
                    Notification.show("Host added successfully", 3000, Notification.Position.BOTTOM_CENTER);
                    hostManager.refreshContent();
                    close();
                } else {
                    Notification.show("Failed to add host", 3000, Notification.Position.BOTTOM_CENTER);
                }
            }
        } else {
            Notification.show("Please fill all fields", 3000, Notification.Position.BOTTOM_CENTER);
        }
    }
}
