package com.qa.selenium;

import com.qa.selenium.pages.JavaScriptAlertsPage;
import org.openqa.selenium.WebDriver;
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

public class JavaScriptAlertsTest {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<JavaScriptAlertsPage> alertsPage = new ThreadLocal<>();
    
    public static WebDriver getDriver() {
        return driver.get();
    }
    
    public static JavaScriptAlertsPage getAlertsPage() {
        return alertsPage.get();
    }
    
    @Parameters({"browser", "headless"})
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser, @Optional("true") String headless) {
        String browserToUse = System.getProperty("browser", browser);
        String headlessToUse = System.getProperty("headless", headless);
        // Quick toggle: set SHOW_BROWSER=true to see browser
        if ("true".equals(System.getenv("SHOW_BROWSER"))) {
            headlessToUse = "false";
        }
        boolean isHeadless = Boolean.parseBoolean(headlessToUse);
        
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
        
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        alertsPage.set(new JavaScriptAlertsPage(getDriver()));
        getAlertsPage().navigateToPage();
    }
    
    @Test(priority = 1)
    public void testJSAlert() {
        getAlertsPage().clickJSAlert();
        String alertText = getAlertsPage().getAlertText();
        Assert.assertEquals(alertText, "I am a JS Alert", "Alert text should match");
        
        getAlertsPage().acceptAlert();
        Assert.assertEquals(getAlertsPage().getResultText(), "You successfully clicked an alert", 
                           "Result should show alert was clicked");
    }
    
    @Test(priority = 2)
    public void testJSConfirmAccept() {
        getAlertsPage().clickJSConfirm();
        getAlertsPage().acceptAlert();
        Assert.assertEquals(getAlertsPage().getResultText(), "You clicked: Ok", 
                           "Result should show OK was clicked");
    }
    
    @Test(priority = 3)
    public void testJSConfirmDismiss() {
        getAlertsPage().clickJSConfirm();
        getAlertsPage().dismissAlert();
        Assert.assertEquals(getAlertsPage().getResultText(), "You clicked: Cancel", 
                           "Result should show Cancel was clicked");
    }
    
    @Test(priority = 4)
    public void testJSPrompt() {
        getAlertsPage().clickJSPrompt();
        getAlertsPage().sendTextToAlert("Test Input");
        Assert.assertEquals(getAlertsPage().getResultText(), "You entered: Test Input", 
                           "Result should show entered text");
    }
    
    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
            alertsPage.remove();
        }
    }
}