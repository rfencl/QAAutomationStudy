package com.qa.selenium;

import org.testng.annotations.*;

public class BaseTest {

    static boolean isHeadless;

    @Parameters({"browser", "headless"})
    @BeforeClass
    public void setupClass(@Optional("chrome") String browser, @Optional("true") String headless) {
        String headlessToUse = System.getProperty("headless", headless);
        // Quick toggle: set SHOW_BROWSER=true to see browser
        if ("true".equals(System.getenv("SHOW_BROWSER"))) {
            headlessToUse = "false";
        }
        isHeadless = Boolean.parseBoolean(headlessToUse);
    }
    
}
