package com.qa.selenium.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import java.time.Duration;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Base Test class for Selenium automation
 * Handles WebDriver setup, teardown, and common configurations
 * Exercise: Create a reusable BaseTest class to manage setup and teardown
 */
public class BaseTest {
    
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Properties config;
    
    // Thread-local driver for parallel execution
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    
    @BeforeClass
    @Parameters({"browser", "headless"})
    public void setupClass(@Optional("chrome") String browser, 
                          @Optional("false") String headless) {
        loadConfiguration();
        setupDriver(browser, Boolean.parseBoolean(headless));
    }
    
    @BeforeMethod
    public void setupMethod() {
        // Additional setup for each test method if needed
        driver = getDriver();
        wait = getWait();
        
        // Navigate to base URL if configured
        String baseUrl = config.getProperty("base.url", "https://example.com");
        if (!baseUrl.isEmpty()) {
            driver.get(baseUrl);
        }
    }
    
    @AfterMethod
    public void teardownMethod() {
        // Clean up after each test method
        if (driver != null) {
            driver.manage().deleteAllCookies();
        }
    }
    
    @AfterClass
    public void teardownClass() {
        quitDriver();
    }
    
    /**
     * Setup WebDriver based on browser parameter
     */
    private void setupDriver(String browser, boolean headless) {
        WebDriver driver = null;
        
        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) {
                    chromeOptions.addArguments("--headless");
                }
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");
                driver = new ChromeDriver(chromeOptions);
                break;
                
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) {
                    firefoxOptions.addArguments("--headless");
                }
                driver = new FirefoxDriver(firefoxOptions);
                break;
                
            case "edge":
                WebDriverManager.edgedriver().setup();
                EdgeOptions edgeOptions = new EdgeOptions();
                if (headless) {
                    edgeOptions.addArguments("--headless");
                }
                driver = new EdgeDriver(edgeOptions);
                break;
                
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
        
        // Configure driver
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        
        // Set thread-local driver
        driverThreadLocal.set(driver);
        
        // Create WebDriverWait
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitThreadLocal.set(wait);
    }
    
    /**
     * Get driver for current thread
     */
    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }
    
    /**
     * Get wait for current thread
     */
    public static WebDriverWait getWait() {
        return waitThreadLocal.get();
    }
    
    /**
     * Quit driver and clean up thread-local
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            waitThreadLocal.remove();
        }
    }
    
    /**
     * Load configuration from properties file
     */
    private void loadConfiguration() {
        config = new Properties();
        try {
            // Try to load from test resources first
            String configPath = "src/test/resources/config.properties";
            FileInputStream fis = new FileInputStream(configPath);
            config.load(fis);
            fis.close();
        } catch (IOException e) {
            // Use default configuration if file not found
            config.setProperty("base.url", "https://the-internet.herokuapp.com");
            config.setProperty("implicit.wait", "10");
            config.setProperty("explicit.wait", "20");
            config.setProperty("page.load.timeout", "30");
        }
    }
    
    /**
     * Get configuration property
     */
    protected String getConfigProperty(String key) {
        return config.getProperty(key);
    }
    
    /**
     * Get configuration property with default value
     */
    protected String getConfigProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    /**
     * Take screenshot for reporting
     */
    public String takeScreenshot(String testName) {
        // Implementation would depend on reporting framework
        // This is a placeholder for screenshot functionality
        return "screenshot_" + testName + "_" + System.currentTimeMillis() + ".png";
    }
    
    /**
     * Wait for page to load completely
     */
    protected void waitForPageLoad() {
        wait.until(driver -> 
            ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
    }
    
    /**
     * Switch to new window/tab
     */
    protected void switchToNewWindow() {
        String originalWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!originalWindow.equals(windowHandle)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
    
    /**
     * Switch back to original window
     */
    protected void switchToOriginalWindow() {
        String originalWindow = driver.getWindowHandles().iterator().next();
        driver.switchTo().window(originalWindow);
    }
    
    /**
     * Execute JavaScript
     */
    protected Object executeJavaScript(String script, Object... args) {
        return ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(script, args);
    }
    
    /**
     * Scroll to element
     */
    protected void scrollToElement(org.openqa.selenium.WebElement element) {
        executeJavaScript("arguments[0].scrollIntoView(true);", element);
    }
    
    /**
     * Highlight element for debugging
     */
    protected void highlightElement(org.openqa.selenium.WebElement element) {
        executeJavaScript("arguments[0].style.border='3px solid red'", element);
    }
}
