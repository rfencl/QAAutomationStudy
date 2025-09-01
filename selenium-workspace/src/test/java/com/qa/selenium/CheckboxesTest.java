package com.qa.selenium;

import com.qa.selenium.pages.CheckboxesPage;
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

public class CheckboxesTest {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<CheckboxesPage> checkboxesPage = new ThreadLocal<>();
    
    public static WebDriver getDriver() {
        return driver.get();
    }
    
    public static CheckboxesPage getCheckboxesPage() {
        return checkboxesPage.get();
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
        
        checkboxesPage.set(new CheckboxesPage(getDriver()));
        getCheckboxesPage().navigateToPage();
    }
    
    @Test(priority = 1)
    public void testCheckboxCount() {
        Assert.assertEquals(getCheckboxesPage().getCheckboxCount(), 2, 
                           "Should have 2 checkboxes");
    }
    
    @Test(priority = 2)
    public void testCheckboxSelection() {
        getCheckboxesPage().clickCheckbox(0);
        Assert.assertTrue(getCheckboxesPage().isCheckboxSelected(0), 
                         "First checkbox should be selected");
    }
    
    @Test(priority = 3)
    public void testCheckboxDeselection() {
        if (getCheckboxesPage().isCheckboxSelected(1)) {
            getCheckboxesPage().clickCheckbox(1);
        }
        Assert.assertFalse(getCheckboxesPage().isCheckboxSelected(1), 
                          "Second checkbox should be deselected");
    }
    
    @AfterMethod
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            driver.remove();
            checkboxesPage.remove();
        }
    }
}