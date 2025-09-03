package com.qa.selenium;

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

public class MouseAndKeysTest {
    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static ThreadLocal<SeleniumDevPage> seleniumDevPage = new ThreadLocal<>();
    private static ThreadLocal<SeleniumDownloadsPage> seleniumDownloadsPage = new ThreadLocal<>();
    private static ThreadLocal<SeleniumHelper> seleniumHelper = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static SeleniumDevPage getSeleniumDevPage() {
        return seleniumDevPage.get();
    }

    public static SeleniumDownloadsPage getSeleniumDownloadsPage() {
        return seleniumDownloadsPage.get();
    }

    @Parameters({ "browser", "headless" })
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
        seleniumDevPage.set(new SeleniumDevPage(driver.get()));
        seleniumDownloadsPage.set(new SeleniumDownloadsPage(driver.get()));
        seleniumHelper.set(new SeleniumHelper(driver.get()));

        // Navigate to SeleniumDevPage
        driver.get().get("https://www.selenium.dev");
    }

    @AfterMethod
    public void tearDown() {
        if (driver.get() != null) {
            driver.get().quit();
        }
    }

    @Test(priority = 1)
    public void testClickLinkWithPageFactory() throws InterruptedException {
        getSeleniumDevPage().clickDownloadsWithPageFactory();
        assertSuccessfulClick();
    }

    @Test(priority = 2)
    public void testClickDownloadsTraditional() throws InterruptedException {
        getSeleniumDevPage().clickDownloadsTraditional();
        assertSuccessfulClick();
    }

    @Test(priority = 3)
    public void testClickDownloadsPerform() throws InterruptedException {
        getSeleniumDevPage().clickDownloadsPerform();
        assertSuccessfulClick();
    }

    private void assertSuccessfulClick() {
        // Verify successful click
        Assert.assertEquals(driver.get().getTitle(),
                "Downloads | Selenium");
    }
}
