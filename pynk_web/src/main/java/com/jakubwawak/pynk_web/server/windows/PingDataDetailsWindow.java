/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.windows;

import com.jakubwawak.pynk_web.entity.PingData;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

/**
 * Window for displaying ping data details
 */
public class PingDataDetailsWindow extends Dialog {
    
    private String width = "80%";
    private String height = "80%";

    VerticalLayout mainLayout;

    private PingData pingData;

    HorizontalLayout header;

    /**
     * Constructor
     * @param pingData
     */
    public PingDataDetailsWindow(PingData pingData) {
        this.pingData = pingData;
        prepareLayout();

        setHeaderTitle("Ping Data Details");
        setWidth(width);
        setHeight(height);
        add(mainLayout);
    }

    /**
     * Prepare the layout
     */
    private void prepareLayout() {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        mainLayout.setAlignItems(Alignment.CENTER);

        prepareHeader();
    }

    /**
     * Prepare the header
     */
    private void prepareHeader() {
        header = new HorizontalLayout();
        header.setSizeFull();
        header.setJustifyContentMode(JustifyContentMode.CENTER);
        header.setAlignItems(Alignment.CENTER);

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

        H1 statusHeader = new H1(pingData.getHostName());
        statusHeader.addClassName("logo");
        statusHeader.getStyle().set("font-family", "monospace");

        Span pending = new Span(pingData.packetStatusCode);
        if (pingData.packetStatusCode.equals("Success")) {
            pending.getElement().getThemeList().add("badge success");
        } else if (pingData.packetStatusCode.equals("Partial loss")) {
            pending.getElement().getThemeList().add("badge contrast");
        } else {
            pending.getElement().getThemeList().add("badge error");
        }

        leftLayout.add(statusHeader);
        rightLayout.add(pending);

        header.add(leftLayout, rightLayout);

        TextArea traceRoute = new TextArea("Trace");
        traceRoute.setValue(pingData.packetTracertData);
        traceRoute.setWidthFull();
        traceRoute.setHeightFull();
        traceRoute.setReadOnly(true);

        TextArea dig = new TextArea("Dig");
        dig.setValue(pingData.packetDigData);
        dig.setWidthFull();
        dig.setHeightFull();
        dig.setReadOnly(true);

        TextArea nslookup = new TextArea("Raw Ping Data");
        nslookup.setValue(pingData.packetRawPing);
        nslookup.setWidthFull();
        nslookup.setHeightFull();
        nslookup.setReadOnly(true);

        HorizontalLayout traceRouteLayout = new HorizontalLayout(traceRoute, dig, nslookup);
        traceRouteLayout.setSizeFull();
        traceRouteLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        traceRouteLayout.setAlignItems(Alignment.CENTER);


        mainLayout.add(header);
        mainLayout.add(new Text("Ping from: " + pingData.getPingTimestamp()));
        mainLayout.add(traceRouteLayout);
    }
    
}
