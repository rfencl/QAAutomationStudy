package com.qa.retry;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import io.github.bonigarcia.wdm.WebDriverManager;

@Listeners(RetryListener.class)
public class RetryTestExample {
    private WebDriver driver;
    private static int networkTimeoutTestCounter = 0;
    private static int elementNotFoundTestCounter = 0;
    
    @BeforeMethod
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test(retryAnalyzer = TestRetryAnalyzer.class)
    public void testNetworkTimeout() {
        networkTimeoutTestCounter++;
        
        // Simulate network timeout - fail first 2 attempts, pass on 3rd
        if (networkTimeoutTestCounter <= 2) {
            throw new org.openqa.selenium.TimeoutException("Simulated network timeout");
        }
        
        driver.get("https://www.google.com");
        Assert.assertTrue(driver.getTitle().contains("Google"));
    }
    
    @Test(retryAnalyzer = TestRetryAnalyzer.class)
    public void testElementNotFound() {
        elementNotFoundTestCounter++;
        
        // Simulate element not found - fail first attempt, pass on 2nd
        if (elementNotFoundTestCounter <= 1) {
            throw new org.openqa.selenium.NoSuchElementException("Simulated element not found");
        }
        
        driver.get("https://www.google.com");
        WebElement searchBox = driver.findElement(By.name("q"));
        Assert.assertTrue(searchBox.isDisplayed());
    }
    
    @Test(retryAnalyzer = TestRetryAnalyzer.class)
    public void testAssertionFailure() {
        // This should NOT retry as AssertionError is not in retryable exceptions
        driver.get("https://www.google.com");
        Assert.assertEquals(driver.getTitle(), "Wrong Title", "This assertion should fail and not retry");
    }
    
    @Test(retryAnalyzer = TestRetryAnalyzer.class)
    public void testSuccessfulTest() {
        // This test should pass on first attempt
        driver.get("https://www.google.com");
        Assert.assertTrue(driver.getTitle().contains("Google"));
    }
}
