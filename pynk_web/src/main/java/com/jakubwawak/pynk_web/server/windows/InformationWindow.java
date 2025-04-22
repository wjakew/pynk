/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web.server.windows;

import com.jakubwawak.pynk_web.PynkWebApplication;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

public class InformationWindow extends Dialog{

    private String width = "50%";
    private String height = "50%";

    VerticalLayout mainLayout;

    /**
     * Constructor
     */
    public InformationWindow() {
        setHeaderTitle("Information");
        setWidth(width);
        setHeight(height);

        mainLayout = new VerticalLayout();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setJustifyContentMode(JustifyContentMode.CENTER);
        mainLayout.setSizeFull();
        StreamResource res = new StreamResource("pynk-logo.png", () -> {
            return InformationWindow.class.getClassLoader().getResourceAsStream("images/pynk_icon.png");
        });

        Image image = new Image(res, "Pynk Logo");
        image.setWidth("5rem");
        image.setHeight("5rem");

        H1 title = new H1("pynk");
        title.addClassName("logo");
        
        Button closeButton = new Button("Close", VaadinIcon.CLOSE.create(),event->close());
        closeButton.addClassName("header-button");

        
        mainLayout.add(image);
        mainLayout.add(title);
        mainLayout.add(new H6(PynkWebApplication.VERSION+"/"+PynkWebApplication.BUILD));
        mainLayout.add(new Text("Pynk is a tool for monitoring your network connections. It allows you to see the status of your network connections and the speed of your internet connection."));
        mainLayout.add(new H6("developed by Jakub Wawak"));
        mainLayout.add(new H6("kubawawak@gmail.com"));
        mainLayout.add(closeButton);

        add(mainLayout);
    }
}
