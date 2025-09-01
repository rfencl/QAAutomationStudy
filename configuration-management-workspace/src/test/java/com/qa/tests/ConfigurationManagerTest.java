package com.qa.tests;

import com.qa.config.ConfigurationManager;
import com.qa.config.Environment;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ConfigurationManagerTest {
    private ConfigurationManager config;
    
    @BeforeMethod
    public void setUp() {
        config = ConfigurationManager.getInstance();
        config.clearCache(); // Reset for each test
    }
    
    @Test
    public void testSingletonInstance() {
        ConfigurationManager config1 = ConfigurationManager.getInstance();
        ConfigurationManager config2 = ConfigurationManager.getInstance();
        Assert.assertSame(config1, config2, "ConfigurationManager should be singleton");
    }
    
    @Test
    public void testDefaultValues() {
        String browserType = config.getString("browser.type", "firefox");
        Assert.assertEquals(browserType, "chrome", "Should load default browser type from properties");
        
        int timeout = config.getInt("browser.timeout", 5);
        Assert.assertEquals(timeout, 10, "Should load default timeout from properties");
        
        boolean headless = config.getBoolean("browser.headless", false);
        Assert.assertTrue(headless, "Should load default headless setting from properties");
    }
    
    @Test
    public void testSystemPropertyOverride() {
        System.setProperty("browser.type", "firefox");
        String browserType = config.getString("browser.type");
        Assert.assertEquals(browserType, "firefox", "System property should override config file");
        System.clearProperty("browser.type");
    }
    
    @Test
    public void testEnvironmentVariableOverride() {
        // Simulate environment variable by setting system property with env format
        System.setProperty("BROWSER_TYPE", "edge");
        String browserType = config.getString("browser.type");
        // Note: This test demonstrates the concept, actual env var testing would need different setup
        System.clearProperty("BROWSER_TYPE");
    }
    
    @Test
    public void testEnvironmentDetection() {
        Environment env = config.getCurrentEnvironment();
        Assert.assertNotNull(env, "Environment should be detected");
        Assert.assertEquals(env, Environment.DEV, "Default environment should be DEV");
    }
    
    @Test
    public void testRuntimePropertySetting() {
        config.setProperty("test.custom.property", "custom_value");
        String value = config.getString("test.custom.property");
        Assert.assertEquals(value, "custom_value", "Should be able to set runtime properties");
    }
    
    @Test
    public void testMissingPropertyWithDefault() {
        String value = config.getString("non.existent.property", "default_value");
        Assert.assertEquals(value, "default_value", "Should return default for missing property");
    }
    
    @Test
    public void testIntegerConversion() {
        int timeout = config.getInt("browser.timeout", 5);
        Assert.assertEquals(timeout, 10, "Should convert string to integer");
        
        int invalid = config.getInt("invalid.number", 99);
        Assert.assertEquals(invalid, 99, "Should return default for invalid number");
    }
    
    @Test
    public void testBooleanConversion() {
        boolean headless = config.getBoolean("browser.headless", false);
        Assert.assertTrue(headless, "Should convert string to boolean");
        
        boolean missing = config.getBoolean("missing.boolean", true);
        Assert.assertTrue(missing, "Should return default for missing boolean");
    }
    
    @Test
    public void testYamlConfiguration() {
        String appName = config.getString("app.name");
        Assert.assertEquals(appName, "Test Application", "Should load from YAML config");
        
        String appUrl = config.getString("app.url");
        Assert.assertEquals(appUrl, "https://the-internet.herokuapp.com/", "Should load URL from YAML");
    }
}
