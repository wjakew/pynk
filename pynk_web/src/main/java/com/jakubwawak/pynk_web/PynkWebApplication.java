/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;
import com.jakubwawak.pynk_web.maintanance.Properties;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;

/**
 * Web application for Pynk data visualization
 */
@SpringBootApplication
@EnableVaadin({"com.jakubwawak.pynk_web"})
@Theme(value = "pynktheme")
public class PynkWebApplication extends SpringBootServletInitializer implements AppShellConfigurator{

	public static String VERSION = "1.0.0";
	public static String BUILD = "pynkweb17042025";

	public static DatabaseEngine databaseEngine;

	public static Properties properties;

	/**
	 * Main method
	 * @param args
	 */
	public static void main(String[] args) {
		showHeader();
		properties = new Properties("pynk.properties");
		if ( properties.fileExists ) {
			properties.parsePropertiesFile();
			databaseEngine = new DatabaseEngine(properties.getValue("databasePath"));
			databaseEngine.connect();
			if ( databaseEngine.connected ) {
				databaseEngine.addLog("web-info", "Starting Pynk Web Application", "info", "#0000FF");
				SpringApplication.run(PynkWebApplication.class, args);
			} else {
				databaseEngine.addLog("web-error", "Failed to connect to database", "error", "#FF0000");
				System.out.println("Failed to connect to database");
			}

		} else {
			System.out.println("Properties file not found, creating default file");
			properties.createPropertiesFile();
			System.out.println("Properties file created, please configure it and restart the application");
		}
	}

	/**
	 * Show header
	 */
	static void showHeader(){
		System.out.println("Pynk Web Application");
		System.out.println("Version: " + VERSION);
		System.out.println("Build: " + BUILD);
		System.out.println("--------------------------------");
	}

}
