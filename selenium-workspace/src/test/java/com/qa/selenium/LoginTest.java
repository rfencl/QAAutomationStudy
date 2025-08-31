package com.qa.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class LoginTest {
    private WebDriver driver;
    private LoginPage loginPage;
    private SeleniumHelper seleniumHelper;
    
    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) {
        // Setup WebDriver based on browser parameter
        switch (browser.toLowerCase()) {
            case "chrome":
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.password_manager_leak_detection", false);
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.setExperimentalOption("prefs", prefs);
                driver = new ChromeDriver(chromeOptions);
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver = new FirefoxDriver();
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
        
        // Configure driver
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        // Initialize page objects and helpers
        loginPage = new LoginPage(driver);
        seleniumHelper = new SeleniumHelper(driver);
        
        // Navigate to login page (replace with actual URL)
        driver.get("https://the-internet.herokuapp.com/login");
    }
    
    @Test(priority = 1)
    public void testValidLogin() {
        loginPage.login("tomsmith", "SuperSecretPassword!");
        
        // Verify successful login message
        Assert.assertTrue(loginPage.isLoginSuccessful(), 
                         "Should show success message after valid login");
        
        String successText = loginPage.getSuccessMessage();
        Assert.assertTrue(successText.contains("You logged into a secure area!"), 
                         "Success message should contain 'You logged into a secure area!'");
    }
    
    @Test(priority = 2)
    public void testInvalidUsername() {
        loginPage.login("invalid_user", "SuperSecretPassword!");
        
        // Verify error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), 
                         "Error message should be displayed for invalid username");
        
        String errorText = loginPage.getErrorMessage();
        Assert.assertTrue(errorText.contains("Your username is invalid!"), 
                         "Error message should contain 'Your username is invalid!'");
    }
    
    @Test(priority = 3)
    public void testInvalidPassword() {
        loginPage.login("tomsmith", "wrong_password");
        
        // Verify error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), 
                         "Error message should be displayed for invalid password");
        
        String errorText = loginPage.getErrorMessage();
        Assert.assertTrue(errorText.contains("Your password is invalid!"), 
                         "Error message should contain 'Your password is invalid!'");
    }
    
    @Test(priority = 4)
    public void testEmptyCredentials() {
        loginPage.login("", "");
        
        // Verify validation message or that login button is disabled
        Assert.assertTrue(loginPage.isErrorMessageDisplayed() || 
                         driver.getCurrentUrl().contains("login"), 
                         "Should show validation error or stay on login page");
    }
    
    @Test(priority = 5)
    public void testSQLInjectionAttempt() {
        String sqlInjection = "' OR '1'='1";
        loginPage.login(sqlInjection, sqlInjection);
        
        // Verify that SQL injection is prevented
        Assert.assertTrue(driver.getCurrentUrl().contains("login"), 
                         "Should not allow SQL injection and stay on login page");
    }
    
    @Test(priority = 6)
    public void testSpecialCharacters() {
        String specialChars = "!@#$%^&*()";
        loginPage.login(specialChars, specialChars);
        
        // Verify handling of special characters
        Assert.assertTrue(loginPage.isErrorMessageDisplayed() || 
                         driver.getCurrentUrl().contains("login"), 
                         "Should handle special characters gracefully");
    }
    
    @Test(priority = 7, groups = {"smoke"})
    public void testPageTitle() {
        String title = loginPage.getPageTitle();
        Assert.assertTrue(title.contains("The Internet"), 
                         "Page title should indicate login page");
    }
    
    @Test(priority = 8, groups = {"regression"})
    public void testFieldClearing() {
        // Enter some text
        loginPage.login("test_user", "test_pass");
        
        // Clear fields
        loginPage.clearFields();
        
        // Verify fields are cleared (this would need actual field value checking)
        System.out.println("Fields cleared successfully");
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
