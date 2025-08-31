package com.qa.selenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import com.qa.selenium.pages.LoginPage;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class LoginTest {
    private LoginPage loginPage;
    private WebDriver driver;
    private WebDriverWait wait;

    @Parameters("browser")
    @BeforeClass
    public void setupClass(@Optional("chrome") String browser) {
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
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                driver = new FirefoxDriver(firefoxOptions);
                break;
            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        // Configure driver
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));

        // Create WebDriverWait
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @BeforeMethod
    public void setupMethod() {
        if (driver != null) {
            // Clear cookies before each test
            driver.manage().deleteAllCookies();

            // Initialize page object
            loginPage = new LoginPage(driver, wait);
            loginPage.navigateToLoginPage();
        }
    }

    @Test(priority = 1)
    public void testValidLogin() {
        loginPage.login("tomsmith", "SuperSecretPassword!");

        // Verify successful login
        Assert.assertTrue(loginPage.isLoginSuccessful(),
                "Should show success message after valid login");
        Assert.assertTrue(loginPage.isLogoutLinkPresent(),
                "Logout link should be present after successful login");
    }

    @Test(priority = 2)
    public void testInvalidLogin() {
        loginPage.login("invalid_user", "wrong_password");

        // Verify error message is displayed
        Assert.assertTrue(loginPage.isLoginFailed(),
                "Error message should be displayed for invalid credentials");

        String errorText = loginPage.getErrorMessage();
        Assert.assertTrue(errorText.contains("invalid") || errorText.contains("incorrect"),
                "Error message should indicate invalid credentials");
    }

    @Test(priority = 3)
    public void testEmptyCredentials() {
        loginPage.login("", "");

        // Verify validation message or that login button is disabled
        Assert.assertTrue(loginPage.isLoginFailed() ||
                driver.getCurrentUrl().contains("login"),
                "Should show validation error or stay on login page");
    }

    @Test(priority = 4)
    public void testSQLInjectionAttempt() {
        String sqlInjection = "' OR '1'='1";
        loginPage.login(sqlInjection, sqlInjection);

        // Verify that SQL injection is prevented
        Assert.assertTrue(driver.getCurrentUrl().contains("login"),
                "Should not allow SQL injection and stay on login page");
    }

    @Test(priority = 5)
    public void testSpecialCharacters() {
        String specialChars = "!@#$%^&*()";
        loginPage.login(specialChars, specialChars);

        // Verify handling of special characters
        Assert.assertTrue(loginPage.isLoginFailed() ||
                driver.getCurrentUrl().contains("login"),
                "Should handle special characters gracefully");
    }

    @Test(priority = 6, groups = { "smoke" })
    public void testPageTitle() {
        String title = loginPage.getPageTitle();
        Assert.assertTrue(title.contains("Login") || title.contains("Sign In"),
                "Page title should indicate login page");
    }

    @Test(priority = 7, groups = { "regression" })
    public void testFieldClearing() {
        // Enter some text
        loginPage.enterUsername("test_user");
        loginPage.enterPassword("test_pass");

        // Clear fields
        loginPage.clearAllFields();

        // Verify fields are cleared
        Assert.assertEquals(loginPage.getCurrentUsername(), "",
                "Username field should be empty after clearing");
        Assert.assertEquals(loginPage.getCurrentPassword(), "",
                "Password field should be empty after clearing");
    }

    @AfterMethod
    public void teardownMethod() {
        // Clean up after each test method
        if (driver != null) {
            driver.manage().deleteAllCookies();
        }
    }

    @AfterClass
    public void teardownClass() {
        if (driver != null) {
            driver.quit();
        }
    }
}
