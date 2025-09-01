package com.qa.tests;

import com.qa.config.Environment;
import com.qa.config.TestConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestConfigTest {
    
    @Test
    public void testBrowserConfiguration() {
        String browserType = TestConfig.getBrowserType();
        Assert.assertNotNull(browserType, "Browser type should not be null");
        Assert.assertEquals(browserType, "chrome", "Default browser should be chrome");
        
        boolean headless = TestConfig.isHeadless();
        Assert.assertTrue(headless, "Default headless should be true");
        
        int timeout = TestConfig.getBrowserTimeout();
        Assert.assertEquals(timeout, 10, "Default timeout should be 10");
    }
    
    @Test
    public void testApplicationConfiguration() {
        String appUrl = TestConfig.getAppUrl();
        Assert.assertNotNull(appUrl, "App URL should not be null");
        Assert.assertTrue(appUrl.startsWith("http"), "App URL should be valid HTTP URL");
        
        String appName = TestConfig.getAppName();
        Assert.assertNotNull(appName, "App name should not be null");
    }
    
    @Test
    public void testDatabaseConfiguration() {
        String dbUrl = TestConfig.getDatabaseUrl();
        Assert.assertNotNull(dbUrl, "Database URL should not be null");
        Assert.assertTrue(dbUrl.startsWith("jdbc:"), "Database URL should be valid JDBC URL");
        
        String username = TestConfig.getDatabaseUsername();
        Assert.assertNotNull(username, "Database username should not be null");
        
        String password = TestConfig.getDatabasePassword();
        Assert.assertNotNull(password, "Database password should not be null");
    }
    
    @Test
    public void testEnvironmentConfiguration() {
        Environment env = TestConfig.getEnvironment();
        Assert.assertNotNull(env, "Environment should not be null");
        
        boolean isDev = TestConfig.isDevEnvironment();
        boolean isProd = TestConfig.isProdEnvironment();
        
        if (env == Environment.DEV) {
            Assert.assertTrue(isDev, "Should correctly identify DEV environment");
            Assert.assertFalse(isProd, "Should not identify as PROD when in DEV");
        } else if (env == Environment.PROD) {
            Assert.assertTrue(isProd, "Should correctly identify PROD environment");
            Assert.assertFalse(isDev, "Should not identify as DEV when in PROD");
        }
    }
    
    @Test
    public void testConfigurationConsistency() {
        // Test that all configuration methods return consistent values
        String browserType1 = TestConfig.getBrowserType();
        String browserType2 = TestConfig.getBrowserType();
        Assert.assertEquals(browserType1, browserType2, "Configuration should be consistent across calls");
        
        String appUrl1 = TestConfig.getAppUrl();
        String appUrl2 = TestConfig.getAppUrl();
        Assert.assertEquals(appUrl1, appUrl2, "App URL should be consistent across calls");
    }
}
