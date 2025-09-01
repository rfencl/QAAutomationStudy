package com.qa.tests;

import com.qa.factory.PageObjectFactory;
import com.qa.factory.WebDriverFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;

public class BaseTest {
    protected WebDriver driver;
    protected PageObjectFactory pageFactory;
    
    @BeforeMethod
    @Parameters({"browser", "headless"})
    public void setUp(String browser, String headless) {
        boolean isHeadless = Boolean.parseBoolean(headless);
        driver = WebDriverFactory.createDriver(browser, isHeadless);
        pageFactory = new PageObjectFactory(driver);
    }
    
    @AfterMethod
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
