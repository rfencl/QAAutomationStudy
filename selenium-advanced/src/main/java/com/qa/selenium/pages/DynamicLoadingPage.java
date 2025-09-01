package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.qameta.allure.Step;

public class DynamicLoadingPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(css = "button")
    private WebElement startButton;
    
    @FindBy(id = "loading")
    private WebElement loadingIndicator;
    
    @FindBy(id = "finish")
    private WebElement finishMessage;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    public DynamicLoadingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(30));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to dynamic loading page 1")
    public DynamicLoadingPage navigateToPage1() {
        driver.get("https://the-internet.herokuapp.com/dynamic_loading/1");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Navigate to dynamic loading page 2")
    public DynamicLoadingPage navigateToPage2() {
        driver.get("https://the-internet.herokuapp.com/dynamic_loading/2");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Click start button")
    public DynamicLoadingPage clickStart() {
        startButton.click();
        return this;
    }
    
    @Step("Wait for loading to complete")
    public DynamicLoadingPage waitForLoadingComplete() {
        wait.until(ExpectedConditions.invisibilityOf(loadingIndicator));
        wait.until(ExpectedConditions.visibilityOf(finishMessage));
        return this;
    }
    
    @Step("Verify finish message is displayed")
    public boolean isFinishMessageDisplayed() {
        try {
            return finishMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Get finish message text")
    public String getFinishMessageText() {
        if (isFinishMessageDisplayed()) {
            return finishMessage.getText();
        }
        return "";
    }
}