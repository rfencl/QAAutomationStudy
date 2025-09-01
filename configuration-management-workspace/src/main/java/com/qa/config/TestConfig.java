package com.qa.config;

public class TestConfig {
    private static final ConfigurationManager config = ConfigurationManager.getInstance();
    
    // Browser Configuration
    public static String getBrowserType() {
        return config.getString("browser.type", "chrome");
    }
    
    public static boolean isHeadless() {
        return config.getBoolean("browser.headless", true);
    }
    
    public static int getBrowserTimeout() {
        return config.getInt("browser.timeout", 10);
    }
    
    // Application Configuration
    public static String getAppUrl() {
        return config.getString("app.url", "https://the-internet.herokuapp.com/");
    }
    
    public static String getAppName() {
        return config.getString("app.name", "Test Application");
    }
    
    // Database Configuration
    public static String getDatabaseUrl() {
        return config.getString("database.url", "jdbc:h2:mem:testdb");
    }
    
    public static String getDatabaseUsername() {
        return config.getString("database.username", "sa");
    }
    
    public static String getDatabasePassword() {
        return config.getString("database.password", "");
    }
    
    // Environment
    public static Environment getEnvironment() {
        return config.getCurrentEnvironment();
    }
    
    // Utility methods
    public static boolean isDevEnvironment() {
        return getEnvironment() == Environment.DEV;
    }
    
    public static boolean isProdEnvironment() {
        return getEnvironment() == Environment.PROD;
    }
}
