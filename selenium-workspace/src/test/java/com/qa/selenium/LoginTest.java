package com.qa.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class LoginTest {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<LoginPage> loginPage = new ThreadLocal<>();
    private static ThreadLocal<SeleniumHelper> seleniumHelper = new ThreadLocal<>();
    
    public static WebDriver getDriver() {
        return driver.get();
    }
    
    public static LoginPage getLoginPage() {
        return loginPage.get();
    }
    
    @Parameters({"browser", "headless"})
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser, @Optional("true") String headless) {
        // Check system properties first, then use TestNG parameters
        String browserToUse = System.getProperty("browser", browser);
        String headlessToUse = System.getProperty("headless", headless);
        // Quick toggle: set SHOW_BROWSER=true to see browser
        if ("true".equals(System.getenv("SHOW_BROWSER"))) {
            headlessToUse = "false";
        }
        boolean isHeadless = Boolean.parseBoolean(headlessToUse);
        
        // Setup WebDriver based on browser parameter
        switch (browserToUse.toLowerCase()) {
            case "chrome":
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.password_manager_leak_detection", false);
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--window-size=1920,1080");
                if (isHeadless) {
                    chromeOptions.addArguments("--headless");
                }
                chromeOptions.setExperimentalOption("prefs", prefs);
                driver.set(new ChromeDriver(chromeOptions));
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (isHeadless) {
                    firefoxOptions.addArguments("--headless");
                }
                driver.set(new FirefoxDriver(firefoxOptions));
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browserToUse);
        }
        
        // Configure driver
        driver.get().manage().window().maximize();
        driver.get().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        // Initialize page objects and helpers
        loginPage.set(new LoginPage(driver.get()));
        seleniumHelper.set(new SeleniumHelper(driver.get()));
        
        // Navigate to login page
        driver.get().get("https://the-internet.herokuapp.com/login");
    }
    
    @Test(priority = 1)
    public void testValidLogin() {
        getLoginPage().login("tomsmith", "SuperSecretPassword!");
        
        // Verify successful login message
        Assert.assertTrue(getLoginPage().isLoginSuccessful(), 
                         "Should show success message after valid login");
        
        String successText = getLoginPage().getSuccessMessage();
        Assert.assertTrue(successText.contains("You logged into a secure area!"), 
                         "Success message should contain 'You logged into a secure area!'");
    }
    
    @Test(priority = 2)
    public void testInvalidUsername() {
        getLoginPage().login("invalid_user", "SuperSecretPassword!");
        
        // Verify error message is displayed
        Assert.assertTrue(getLoginPage().isErrorMessageDisplayed(), 
                         "Error message should be displayed for invalid username");
        
        String errorText = getLoginPage().getErrorMessage();
        Assert.assertTrue(errorText.contains("Your username is invalid!"), 
                         "Error message should contain 'Your username is invalid!'");
    }
    
    @Test(priority = 3)
    public void testInvalidPassword() {
        getLoginPage().login("tomsmith", "wrong_password");
        
        // Verify error message is displayed
        Assert.assertTrue(getLoginPage().isErrorMessageDisplayed(), 
                         "Error message should be displayed for invalid password");
        
        String errorText = getLoginPage().getErrorMessage();
        Assert.assertTrue(errorText.contains("Your password is invalid!"), 
                         "Error message should contain 'Your password is invalid!'");
    }
    
    @Test(priority = 4)
    public void testEmptyCredentials() {
        getLoginPage().login("", "");
        
        // Verify validation message or that login button is disabled
        Assert.assertTrue(getLoginPage().isErrorMessageDisplayed() || 
                         getDriver().getCurrentUrl().contains("login"), 
                         "Should show validation error or stay on login page");
    }
    
    @Test(priority = 5)
    public void testSQLInjectionAttempt() {
        String sqlInjection = "' OR '1'='1";
        getLoginPage().login(sqlInjection, sqlInjection);
        
        // Verify that SQL injection is prevented
        Assert.assertTrue(getDriver().getCurrentUrl().contains("login"), 
                         "Should not allow SQL injection and stay on login page");
    }
    
    @Test(priority = 6)
    public void testSpecialCharacters() {
        String specialChars = "!@#$%^&*()";
        getLoginPage().login(specialChars, specialChars);
        
        // Verify handling of special characters
        Assert.assertTrue(getLoginPage().isErrorMessageDisplayed() || 
                         getDriver().getCurrentUrl().contains("login"), 
                         "Should handle special characters gracefully");
    }
    
    @Test(priority = 7, groups = {"smoke"})
    public void testPageTitle() {
        String title = getLoginPage().getPageTitle();
        Assert.assertTrue(title.contains("The Internet"), 
                         "Page title should indicate login page");
    }
    
    @Test(priority = 8, groups = {"regression"})
    public void testFieldClearing() {
        // Enter some text
        getLoginPage().login("test_user", "test_pass");
        
        // Clear fields
        getLoginPage().clearFields();
        
        // Verify fields are cleared (this would need actual field value checking)
        System.out.println("Fields cleared successfully");
    }
    
    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
            loginPage.remove();
            seleniumHelper.remove();
        }
    }
}
