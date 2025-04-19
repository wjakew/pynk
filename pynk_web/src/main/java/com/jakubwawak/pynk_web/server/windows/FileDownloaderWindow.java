/**
 * by Jakub Wawak
 * all rights reserved
 * kubawawak@gmail.com / j.wawak@usp.pl
 */
package com.jakubwawak.pynk_web.server.windows;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Object for creating component for downloading file
 */
public class FileDownloaderWindow {

    public Dialog dialog;
    VerticalLayout main_layout;

    Button close_button;

    File to_download;

    /**
     * Constructor
     */
    public FileDownloaderWindow(File file_to_download) {
        dialog = new Dialog();
        this.to_download = file_to_download;
        main_layout = new VerticalLayout();
        close_button = new Button("Close Window", this::close_action);
        prepare_dialog();
    }

    /**
     * Function for loading components
     */
    void prepare_dialog() {
        main_layout.add(new H3("Merge File Download"));
        main_layout.add(new Text("To download the file, click on the link below:"));
        addLinkToFile(to_download);
        main_layout.add(new Text("Be careful! The file may contain sensitive data!"));
        main_layout.add(close_button);

        main_layout.setSizeFull();
        main_layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        main_layout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        main_layout.getStyle().set("text-align", "center");
        dialog.add(main_layout);
    }

    /**
     * Function for adding link to file
     * 
     * @param file
     */
    private void addLinkToFile(File file) {
        StreamResource streamResource = new StreamResource(file.getName(), () -> getStream(file));
        Anchor link = new Anchor(streamResource, String.format("%s (%d KB)", file.getName(),
                (int) file.length() / 1024));
        link.getElement().setAttribute("download", true);
        main_layout.add(link);
    }

    /**
     * Function for loading stream of file to object
     * 
     * @param file
     * @return InputStream
     */
    private InputStream getStream(File file) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.out.println("Failed to load stream file: " + e.toString());
        }
        return stream;
    }

    /**
     * Function for closing dialog window
     * 
     * @param e
     */
    private void close_action(ClickEvent e) {
        dialog.close();
    }
}