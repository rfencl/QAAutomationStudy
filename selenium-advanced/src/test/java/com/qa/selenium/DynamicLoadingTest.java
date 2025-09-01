package com.qa.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import com.qa.selenium.pages.DynamicLoadingPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DynamicLoadingTest {
    private DynamicLoadingPage dynamicPage;
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();

    @Parameters({"browser", "headless"})
    @BeforeClass
    public void setupClass(@Optional("chrome") String browser, @Optional("false") String headless) {
        WebDriver driver = createDriver(browser, Boolean.parseBoolean(headless));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driverThreadLocal.set(driver);
        waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(30)));
    }
    
    private WebDriver createDriver(String browser, boolean headless) {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.password_manager_leak_detection", false);
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        if (headless) {
            chromeOptions.addArguments("--headless");
        }
        chromeOptions.setExperimentalOption("prefs", prefs);
        return new ChromeDriver(chromeOptions);
    }
    
    private WebDriver getDriver() { return driverThreadLocal.get(); }
    private WebDriverWait getWait() { return waitThreadLocal.get(); }

    @Test
    public void testDynamicLoading1() {
        dynamicPage = new DynamicLoadingPage(getDriver());
        dynamicPage.navigateToPage1()
                  .clickStart()
                  .waitForLoadingComplete();
        
        Assert.assertTrue(dynamicPage.isFinishMessageDisplayed(), "Finish message should be displayed");
        Assert.assertEquals(dynamicPage.getFinishMessageText(), "Hello World!", "Should show Hello World message");
    }

    @Test
    public void testDynamicLoading2() {
        dynamicPage = new DynamicLoadingPage(getDriver());
        dynamicPage.navigateToPage2()
                  .clickStart()
                  .waitForLoadingComplete();
        
        Assert.assertTrue(dynamicPage.isFinishMessageDisplayed(), "Finish message should be displayed");
        Assert.assertEquals(dynamicPage.getFinishMessageText(), "Hello World!", "Should show Hello World message");
    }

    @AfterClass
    public void teardownClass() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove();
            waitThreadLocal.remove();
        }
    }
}