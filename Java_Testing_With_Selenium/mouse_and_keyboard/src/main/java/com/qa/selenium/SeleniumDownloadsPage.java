package com.qa.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

/**
 * Page Object Model (POM) implementation for Selenium Dev Page
 */
public class SeleniumDownloadsPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(linkText = "Downloads")
    private WebElement downloadsLink;

    // Traditional locators (without Page Factory)
    private By downloadsLnk = By.linkText("Downloads");

    public SeleniumDownloadsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    // Method using Page Factory elements
    public void clickDownloadsWithPageFactory() throws InterruptedException {
        downloadsLink.click();
        wait(1000);
    }

}
