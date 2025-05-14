/**
 * by Jakub Wawak
 * kubawawak@gmail.com
 * all rights reserved
 */
package com.jakubwawak.maintanance;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Function for managing and creating properties file
 */
public class Properties {

    int INTEGRITY_CHECK_FLAG = 6;
    String EXPECTED_FILEVERSION = "1";

    String propertiesFile;

    public boolean error;
    public boolean integrityFlag;
    public boolean fileVersionFlag;
    public String propertiesFileVersion;

    ArrayList<PropertiesField> propertiesCollection;
    public boolean fileExists;

    /**
     * Constructor
     * 
     * @param propertiesFile
     */
    public Properties(String propertiesFile) {
        this.propertiesFile = propertiesFile;
        propertiesCollection = new ArrayList<>();
        error = false;

        // flag for checking
        File checkFile = new File(propertiesFile);
        fileExists = checkFile.exists();
    }

    /**
     * Function for getting properties value from collection by key given
     * 
     * @param key
     * @return String
     */
    public String getValue(String key) {
        for (PropertiesField pf : propertiesCollection) {
            if (pf.propertiesKey.contains(key)) {
                return pf.propertiesValue;
            }
        }
        return null;
    }

    /**
     * Function for parsing properties file
     */
    public void parsePropertiesFile() {
        File configurationFile = new File(propertiesFile);
        if (configurationFile.exists()) {
            try {
                Scanner propertiesScanner = new Scanner(configurationFile);
                while (propertiesScanner.hasNextLine()) {
                    String line = propertiesScanner.nextLine();

                    if (line.startsWith("$")) {
                        // getting file version
                        if (line.contains("$fileVersion")) {
                            propertiesFileVersion = valueOfKey(line);
                            fileVersionFlag = propertiesFileVersion.equals(EXPECTED_FILEVERSION);
                        } else {
                            propertiesCollection
                                    .add(new PropertiesField(line.split("=")[0].replaceAll("$", ""), valueOfKey(line)));
                        }
                    }
                }
                System.out.println("PROPERTIES-PARSER - Loaded " + propertiesCollection.size()
                        + " properties from configuration file.");
                integrityFlag = propertiesCollection.size() == INTEGRITY_CHECK_FLAG;
                propertiesScanner.close();
            } catch (Exception ex) {
                System.out.println("PROPERTIES-PARSER-FAILED - Failed to parse (" + ex.toString() + ")");
            }

        }
    }

    /**
     * Function for retriving everyting after the "=" in string
     * 
     * @param content
     * @return String
     */
    String valueOfKey(String content) {
        // Find the index of the "=" sign.
        int indexEquals = content.indexOf('=');

        // If the "=" sign is not found, return null.
        if (indexEquals == -1) {
            return null;
        }
        // Return the substring after the "=" sign.
        return content.substring(indexEquals + 1);
    }

    /**
     * Function for creating properties file
     */
    public void createPropertiesFile() {
        try {
            FileWriter writer = new FileWriter(propertiesFile);
            writer.write("#pynk properties file\n");
            writer.write("$fileVersion=1\n");
            writer.write("#by Jakub Wawak / all rights reserved / kubawawak@gmail.com\n");
            writer.write("$databasePath=./pynk.db\n");
            writer.write("$databaseType=mongodb\n");
            writer.write("#allowed values: sqlite, mongodb\n");
            writer.write("$databaseUrl=mongodb://localhost:27017\n");
            writer.write("#trace route settings\n");
            writer.write("$traceRouteInterval=10\n");
            writer.write("#trace route target\n");
            writer.write("$traceRouteTarget=8.8.8.8\n");
            writer.write("#hourly ping settings\n");
            writer.write("$pingInterval=60\n");
            writer.write("#hourly ping target\n");
            writer.write("$pingTarget=8.8.8.8\n");
            writer.close();
        } catch (Exception ex) {
            error = true;
            System.out.println("PROPERTIES - Failed to create .properties file (" + ex.toString() + ")");
        }
    }
}