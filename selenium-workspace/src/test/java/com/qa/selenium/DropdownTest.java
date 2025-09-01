package com.qa.selenium;

import com.qa.selenium.pages.DropdownPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.Assert;
import org.testng.annotations.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DropdownTest {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<DropdownPage> dropdownPage = new ThreadLocal<>();
    
    public static WebDriver getDriver() {
        return driver.get();
    }
    
    public static DropdownPage getDropdownPage() {
        return dropdownPage.get();
    }
    
    @Parameters("browser")
    @BeforeMethod
    public void setUp(@Optional("chrome") String browser) {
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
                driver.set(new ChromeDriver(chromeOptions));
                break;
            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                driver.set(new FirefoxDriver());
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }
        
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        dropdownPage.set(new DropdownPage(getDriver()));
        getDropdownPage().navigateToPage();
    }
    
    @Test(priority = 1)
    public void testSelectOption1() {
        getDropdownPage().selectByValue("1");
        Assert.assertEquals(getDropdownPage().getSelectedOption(), "Option 1", 
                           "Should select Option 1");
    }
    
    @Test(priority = 2)
    public void testSelectOption2() {
        getDropdownPage().selectByValue("2");
        Assert.assertEquals(getDropdownPage().getSelectedOption(), "Option 2", 
                           "Should select Option 2");
    }
    
    @Test(priority = 3)
    public void testSelectByText() {
        getDropdownPage().selectByText("Option 1");
        Assert.assertEquals(getDropdownPage().getSelectedOption(), "Option 1", 
                           "Should select Option 1 by text");
    }
    
    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
            dropdownPage.remove();
        }
    }
}