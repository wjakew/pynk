/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.components;

import java.util.ArrayList;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;
import com.jakubwawak.pynk_web.entity.Host;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Host Management Component
 */
public class HostManagementComponent extends VerticalLayout{
    
    ArrayList<Host> content;
    Grid<Host> hostGrid;

    DatabaseEngine databaseEngine;

    Button addHostButton,refreshButton;

    FlexLayout leftLayout,rightLayout;
    TextField searchTextField;
    HorizontalLayout headerLayout;
    
    /**
     * Constructor
     */
    public HostManagementComponent(){
        addClassName("host-management-component");
        databaseEngine = PynkWebApplication.databaseEngine;

        prepareContent();
        prepareLayout();

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        
    }

    /**
     * Refresh content
     */
    public void refreshContent(){
        content.clear();
        content.addAll(databaseEngine.getHosts());
        hostGrid.getDataProvider().refreshAll();
    }

    /**
     * Prepare content
     */
    void prepareContent(){

        headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(JustifyContentMode.START);
        headerLayout.setAlignItems(Alignment.CENTER);

        leftLayout = new FlexLayout();
        leftLayout.setWidthFull();
        leftLayout.setJustifyContentMode(JustifyContentMode.START);
        leftLayout.setAlignItems(Alignment.CENTER);

        rightLayout = new FlexLayout();
        rightLayout.setWidthFull();
        rightLayout.setJustifyContentMode(JustifyContentMode.END);
        rightLayout.setAlignItems(Alignment.CENTER);

        addHostButton = new Button("Add Host",VaadinIcon.PLUS.create());
        addHostButton.addClassName("header-button");

        refreshButton = new Button("",VaadinIcon.REFRESH.create());
        refreshButton.addClassName("header-button");
        refreshButton.addClickListener(event -> {
            refreshContent();
        });

        H4 logo = new H4("hosts");
        logo.addClassName("logo");
        logo.getStyle().set("margin-left", "10px");

        GridListDataView<Host> dataView = hostGrid.setItems(content);

        dataView.addFilter(host -> {
            if (searchTextField.getValue() == null || searchTextField.getValue().isEmpty()){
                return true;
            }
            boolean result = false;
            if (host.getHostName().toLowerCase().contains(searchTextField.getValue().toLowerCase())){
                result = true;
            }
            if (host.getHostIp().toLowerCase().contains(searchTextField.getValue().toLowerCase())){
                result = true;
            }
            if (host.getHostCategory().toLowerCase().contains(searchTextField.getValue().toLowerCase())){
                result = true;
            }
            if (host.getHostStatus().toLowerCase().contains(searchTextField.getValue().toLowerCase())){
                result = true;
            }
            return result;
        });

        searchTextField = new TextField();
        searchTextField.setWidth("100%");
        searchTextField.setPlaceholder("search");
        searchTextField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchTextField.getStyle().set("margin-left", "20px");
        searchTextField.setValueChangeMode(ValueChangeMode.EAGER);
        searchTextField.addValueChangeListener(e -> dataView.refreshAll());

        rightLayout.add(addHostButton,refreshButton);
        leftLayout.add(logo,searchTextField);

        headerLayout.add(leftLayout,rightLayout);

        content = new ArrayList<>();
        
        content.addAll(databaseEngine.getHosts());
        
        hostGrid = new Grid<>(Host.class,false);

        hostGrid.addColumn(Host::getHostName).setHeader("Host Name");
        hostGrid.addColumn(Host::getHostIp).setHeader("Host IP");

        hostGrid.addColumn(new ComponentRenderer<Component,Host>(host ->{
            TextField textField = new TextField();
            textField.setValue(Integer.toString(host.getHostJobTime()));
            textField.setWidth("100%");
            textField.setReadOnly(true);

            textField.addValueChangeListener(event -> {
                host.setHostJobTime(Integer.parseInt(textField.getValue()));
                databaseEngine.updateHost(host);
                Notification.show("Host job (" + host.getHostName() + ") time updated");
            });
            return textField;
        })).setHeader("Host Name");

        hostGrid.addColumn(new ComponentRenderer<Component,Host>(host ->{
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(databaseEngine.getAllUniqueHostCategories());
            comboBox.setValue(host.getHostCategory());
            comboBox.setWidth("100%");
            comboBox.setReadOnly(false);

            comboBox.addValueChangeListener(event -> {
                host.setHostCategory(comboBox.getValue());
                databaseEngine.updateHost(host);
                Notification.show("Host (" + host.getHostName() + ") category updated");
            });
            return comboBox;
        })).setHeader("Category");

        hostGrid.addColumn(new ComponentRenderer<Component,Host>(host ->{
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.setItems(databaseEngine.getAllUniqueHostStatuses());
            comboBox.setValue(host.getHostStatus());
            comboBox.setWidth("100%");
            comboBox.setReadOnly(false);

            comboBox.addValueChangeListener(event -> {
                host.setHostStatus(comboBox.getValue());
                databaseEngine.updateHost(host);
                Notification.show("Host (" + host.getHostName() + ") status updated");
            });

            return comboBox;
        })).setHeader("Status");

        hostGrid.addColumn(new ComponentRenderer<Component,Host>(host ->{
            Button button = new Button("",VaadinIcon.TRASH.create());
            button.addThemeVariants(ButtonVariant.LUMO_ERROR,ButtonVariant.LUMO_SMALL);
            button.addClickListener(event -> {
                int ans = databaseEngine.deleteHost(host.getHostId());
                if(ans > 0){
                    refreshContent();
                    Notification.show("Host (" + host.getHostName() + ") deleted");
                }else{
                    Notification.show("Host (" + host.getHostName() + ") deletion failed");
                }
            });
            return button;
        })).setHeader("Actions");

        hostGrid.setSizeFull();
        hostGrid.setItems(content);   
    }

    /**
     * Prepare layout
     */
    void prepareLayout(){
        add(headerLayout);
        add(hostGrid);
    }
}
