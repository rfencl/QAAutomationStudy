package com.qa.tests;

import com.qa.config.ConfigurationManager;
import com.qa.config.TestConfig;
import org.testng.annotations.Test;

public class ConfigurationDemoTest {
    
    @Test
    public void demonstrateConfigurationManagement() {
        System.out.println("=== Configuration Management Demo ===");
        
        // Show current environment
        System.out.println("Current Environment: " + TestConfig.getEnvironment());
        
        // Show browser configuration
        System.out.println("\n--- Browser Configuration ---");
        System.out.println("Browser Type: " + TestConfig.getBrowserType());
        System.out.println("Headless Mode: " + TestConfig.isHeadless());
        System.out.println("Timeout: " + TestConfig.getBrowserTimeout() + " seconds");
        
        // Show application configuration
        System.out.println("\n--- Application Configuration ---");
        System.out.println("App URL: " + TestConfig.getAppUrl());
        System.out.println("App Name: " + TestConfig.getAppName());
        
        // Show database configuration
        System.out.println("\n--- Database Configuration ---");
        System.out.println("Database URL: " + TestConfig.getDatabaseUrl());
        System.out.println("Database Username: " + TestConfig.getDatabaseUsername());
        
        // Show environment-specific behavior
        System.out.println("\n--- Environment-Specific Behavior ---");
        if (TestConfig.isDevEnvironment()) {
            System.out.println("Running in DEV mode - Debug logging enabled");
        } else if (TestConfig.isProdEnvironment()) {
            System.out.println("Running in PROD mode - Minimal logging");
        } else {
            System.out.println("Running in " + TestConfig.getEnvironment() + " mode");
        }
        
        // Demonstrate runtime configuration
        System.out.println("\n--- Runtime Configuration ---");
        ConfigurationManager config = ConfigurationManager.getInstance();
        config.setProperty("test.execution.id", "DEMO-12345");
        config.setProperty("test.start.time", String.valueOf(System.currentTimeMillis()));
        
        System.out.println("Test Execution ID: " + config.getString("test.execution.id"));
        System.out.println("Test Start Time: " + config.getString("test.start.time"));
        
        // Show configuration priority
        System.out.println("\n--- Configuration Priority Demo ---");
        System.out.println("Note: Run with -Dbrowser.type=firefox to see system property override");
        System.out.println("Note: Run with -Denv=qa to see environment-specific configuration");
        
        System.out.println("\n=== Demo Complete ===");
    }
}
