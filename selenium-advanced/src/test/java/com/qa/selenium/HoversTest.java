package com.qa.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import com.qa.selenium.pages.HoversPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class HoversTest {
    private HoversPage hoversPage;
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();

    @Parameters("browser")
    @BeforeClass
    public void setupClass(@Optional("chrome") String browser) {
        WebDriver driver = createDriver(browser);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driverThreadLocal.set(driver);
        waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(20)));
    }
    
    private WebDriver createDriver(String browser) {
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.password_manager_leak_detection", false);
        WebDriverManager.chromedriver().setup();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
        chromeOptions.setExperimentalOption("prefs", prefs);
        return new ChromeDriver(chromeOptions);
    }
    
    private WebDriver getDriver() { return driverThreadLocal.get(); }
    private WebDriverWait getWait() { return waitThreadLocal.get(); }

    @BeforeMethod
    public void setupMethod() {
        hoversPage = new HoversPage(getDriver());
        hoversPage.navigateToPage();
    }

    @Test
    public void testHoverFirstFigure() {
        hoversPage.hoverOverFigure(0);
        Assert.assertTrue(hoversPage.isCaptionDisplayed(0), "Caption should be displayed on hover");
        Assert.assertTrue(hoversPage.getCaptionText(0).contains("user1"), "Should show user1 caption");
    }

    @Test
    public void testHoverSecondFigure() {
        hoversPage.hoverOverFigure(1);
        Assert.assertTrue(hoversPage.isCaptionDisplayed(1), "Caption should be displayed on hover");
        Assert.assertTrue(hoversPage.getCaptionText(1).contains("user2"), "Should show user2 caption");
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