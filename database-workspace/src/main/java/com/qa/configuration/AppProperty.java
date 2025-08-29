package com.qa.configuration;

import java.io.IOException;
import java.io.InputStream;

public class AppProperty {

    /**
     * Force Unit tests to use the main properties
     * 
     * @param propertyFileName
     * @return
     */
    public static void getProperties(String propertyFileName) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream input = loader.getResourceAsStream("../classes/" + propertyFileName);
        try {
            if (input == null)
                input = loader.getResourceAsStream("resources/" + propertyFileName);
            DatabaseProperty.getProperties().load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
