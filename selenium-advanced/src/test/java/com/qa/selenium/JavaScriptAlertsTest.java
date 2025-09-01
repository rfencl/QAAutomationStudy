package com.qa.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;
import com.qa.selenium.pages.JavaScriptAlertsPage;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class JavaScriptAlertsTest extends BaseTest {
    private JavaScriptAlertsPage alertsPage;
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();

    @Parameters({"browser", "headless"})
    @BeforeClass
    public void setupClass(@Optional("chrome") String browser, @Optional("true") String headless) {
        super.setupClass(browser, headless);
        WebDriver driver = createDriver(browser, isHeadless);        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driverThreadLocal.set(driver);
        waitThreadLocal.set(new WebDriverWait(driver, Duration.ofSeconds(20)));
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

    @BeforeMethod
    public void setupMethod() {
        alertsPage = new JavaScriptAlertsPage(getDriver());
        alertsPage.navigateToPage();
    }

    @Test
    public void testJSAlert() {
        alertsPage.clickAlertButton();
        String alertText = alertsPage.getAlertText();
        Assert.assertEquals(alertText, "I am a JS Alert", "Alert text should match");
        alertsPage.acceptAlert();
        Assert.assertEquals(alertsPage.getResultText(), "You successfully clicked an alert");
    }

    @Test
    public void testJSConfirmAccept() {
        alertsPage.clickConfirmButton();
        alertsPage.acceptAlert();
        Assert.assertEquals(alertsPage.getResultText(), "You clicked: Ok");
    }

    @Test
    public void testJSConfirmDismiss() {
        alertsPage.clickConfirmButton();
        alertsPage.dismissAlert();
        Assert.assertEquals(alertsPage.getResultText(), "You clicked: Cancel");
    }

    @Test
    public void testJSPrompt() {
        alertsPage.clickPromptButton();
        alertsPage.enterTextInPrompt("Test Input");
        Assert.assertEquals(alertsPage.getResultText(), "You entered: Test Input");
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