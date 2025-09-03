package com.qa.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

/**
 * Page Object Model (POM) implementation for Selenium Dev Page
 */
public class SeleniumDevPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(linkText = "Downloads")
    private WebElement downloadsLink;

    // Traditional locators (without Page Factory)
    private By downloadsLnk = By.linkText("Downloads");

    public SeleniumDevPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        PageFactory.initElements(driver, this);
    }

    // Method using Page Factory elements
    public void clickDownloadsWithPageFactory() throws InterruptedException {
        downloadsLink.click();
        Thread.sleep(1000);
    }

    // Method using traditional locators
    public void clickDownloadsTraditional() {

        // Wait for login button to be clickable then click
        WebElement downloadsLink = wait.until(ExpectedConditions.elementToBeClickable(downloadsLnk));
        downloadsLink.click();

        // Wait a moment for the page to process the login
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void clickDownloadsPerform() {
        Actions actions = new Actions(driver);
        actions.moveToElement(downloadsLink)
                .click()
                .pause(1000)
                .perform();
        // // Wait a moment for the page to load
        // try {
        // Thread.sleep(1000);
        // } catch (InterruptedException e) {
        // Thread.currentThread().interrupt();
        // }
    }

}
