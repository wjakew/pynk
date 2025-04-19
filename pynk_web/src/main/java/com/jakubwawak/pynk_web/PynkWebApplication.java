/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.pynk_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import com.jakubwawak.pynk_web.database_engine.DatabaseEngine;
import com.jakubwawak.pynk_web.maintanance.Properties;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import com.vaadin.flow.theme.Theme;

import jakarta.annotation.PreDestroy;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Web application for Pynk data visualization
 */
@SpringBootApplication
@EnableVaadin({ "com.jakubwawak.pynk_web" })
@Theme(value = "pynktheme")
public class PynkWebApplication extends SpringBootServletInitializer implements AppShellConfigurator {

	public static String VERSION = "1.0.0";
	public static String BUILD = "pynkweb19042025";

	public static DatabaseEngine databaseEngine;
	public static Properties properties;
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Bean
	@Scope("singleton")
	public static DatabaseEngine getDatabaseEngine() {
		return databaseEngine;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		showHeader();
		properties = new Properties("pynk.properties");
		if (properties.fileExists) {
			properties.parsePropertiesFile();
			initializeDatabase();
			SpringApplication.run(PynkWebApplication.class);
		} else {
			System.out.println("Properties file not found, creating default file");
			properties.createPropertiesFile();
			System.out.println("Properties file created, please configure it and restart the application");
		}
	}

	/**
	 * Initialize database connection
	 */
	private static void initializeDatabase() {
		databaseEngine = new DatabaseEngine(properties.getValue("databasePath"));
		connectToDatabase();
		scheduleDatabaseReconnect();
	}

	/**
	 * Connect to database with retry logic
	 */
	private static void connectToDatabase() {
		final int MAX_RETRIES = 3;
		int retryCount = 0;
		boolean connected = false;

		while (!connected && retryCount < MAX_RETRIES) {
			try {
				databaseEngine.connect();
				if (databaseEngine.connected) {
					connected = true;
					databaseEngine.addLog("database-info", "Connected to database", "info", "#0000FF");
				} else {
					retryCount++;
					if (retryCount < MAX_RETRIES) {
						System.out.println("Failed to connect to database, retrying in 5 seconds...");
						Thread.sleep(5000);
					}
				}
			} catch (Exception e) {
				retryCount++;
				if (retryCount < MAX_RETRIES) {
					System.out
							.println("Error connecting to database: " + e.getMessage() + ", retrying in 5 seconds...");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		if (!connected) {
			databaseEngine.addLog("database-error", "Failed to connect to database after " + MAX_RETRIES + " attempts",
					"error", "#FF0000");
			System.out.println("Failed to connect to database after " + MAX_RETRIES + " attempts");
		}
	}

	/**
	 * Schedule database reconnect with improved error handling
	 */
	private static void scheduleDatabaseReconnect() {
		scheduler.scheduleAtFixedRate(() -> {
			try {
				if (!databaseEngine.connected) {
					System.out.println("Database connection lost, attempting to reconnect...");
					connectToDatabase();
				}
			} catch (Exception e) {
				System.out.println("Error in scheduled database reconnection: " + e.getMessage());
			}
		}, 1, 1, TimeUnit.HOURS);
	}

	@PreDestroy
	public void cleanup() {
		if (scheduler != null && !scheduler.isShutdown()) {
			scheduler.shutdown();
			try {
				if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
					scheduler.shutdownNow();
				}
			} catch (InterruptedException e) {
				scheduler.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		if (databaseEngine != null) {
			databaseEngine.closeConnection();
		}
	}

	/**
	 * Function for configuring the page
	 * 
	 * @param settings
	 */
	@Override
	public void configurePage(AppShellSettings settings) {
		settings.setViewport("width=device-width, initial-scale=1");
		settings.addMetaTag("author", "Jakub Wawak");
		settings.addFavIcon("icon", "logo.ico", "192x192");
		settings.addLink("shortcut icon", "logo.ico");
	}

	/**
	 * Show header
	 */
	static void showHeader() {
		System.out.println("Pynk Web Application");
		System.out.println("Version: " + VERSION);
		System.out.println("Build: " + BUILD);
		System.out.println("--------------------------------");
	}

}
