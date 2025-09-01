package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.Alert;
import io.qameta.allure.Step;

public class JavaScriptAlertsPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(css = "button[onclick='jsAlert()']")
    private WebElement alertButton;
    
    @FindBy(css = "button[onclick='jsConfirm()']")
    private WebElement confirmButton;
    
    @FindBy(css = "button[onclick='jsPrompt()']")
    private WebElement promptButton;
    
    @FindBy(id = "result")
    private WebElement result;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    public JavaScriptAlertsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to JavaScript alerts page")
    public JavaScriptAlertsPage navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Click alert button")
    public JavaScriptAlertsPage clickAlertButton() {
        alertButton.click();
        return this;
    }
    
    @Step("Click confirm button")
    public JavaScriptAlertsPage clickConfirmButton() {
        confirmButton.click();
        return this;
    }
    
    @Step("Click prompt button")
    public JavaScriptAlertsPage clickPromptButton() {
        promptButton.click();
        return this;
    }
    
    @Step("Accept alert")
    public JavaScriptAlertsPage acceptAlert() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
        return this;
    }
    
    @Step("Dismiss alert")
    public JavaScriptAlertsPage dismissAlert() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.dismiss();
        return this;
    }
    
    @Step("Enter text in prompt: {text}")
    public JavaScriptAlertsPage enterTextInPrompt(String text) {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.sendKeys(text);
        alert.accept();
        return this;
    }
    
    @Step("Get alert text")
    public String getAlertText() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        return alert.getText();
    }
    
    @Step("Get result text")
    public String getResultText() {
        wait.until(ExpectedConditions.visibilityOf(result));
        return result.getText();
    }
}