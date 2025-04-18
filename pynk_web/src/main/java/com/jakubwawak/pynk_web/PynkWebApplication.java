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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Web application for Pynk data visualization
 */
@SpringBootApplication
@EnableVaadin({"com.jakubwawak.pynk_web"})
@Theme(value = "pynktheme")
public class PynkWebApplication extends SpringBootServletInitializer implements AppShellConfigurator{

	public static String VERSION = "1.0.0";
	public static String BUILD = "pynkweb18042025";

	public static DatabaseEngine databaseEngine;

	public static Properties properties;

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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
			connectToDatabase();
			scheduleDatabaseReconnect();
			SpringApplication.run(PynkWebApplication.class);
		} else {
			System.out.println("Properties file not found, creating default file");
			properties.createPropertiesFile();
			System.out.println("Properties file created, please configure it and restart the application");
		}
	}

	/**
	 * Connect to database
	 */
	private static void connectToDatabase() {
		databaseEngine.connect();
		if ( databaseEngine.connected ) {
			databaseEngine.addLog("database-info", "Connected to database", "info", "#0000FF");
		} else {
			databaseEngine.addLog("database-error", "Failed to connect to database", "error", "#FF0000");
			System.out.println("Failed to connect to database");
		}
	}

	/**
	 * Schedule database reconnect
	 */
	private static void scheduleDatabaseReconnect() {
		scheduler.scheduleAtFixedRate(() -> {
			if ( !databaseEngine.connected ) {
				System.out.println("Attempting to reconnect to the database...");
				connectToDatabase();
			}
		}, 1, 1, TimeUnit.HOURS);
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
