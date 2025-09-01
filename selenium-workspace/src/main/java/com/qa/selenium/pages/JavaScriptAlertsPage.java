package com.qa.selenium.pages;

import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class JavaScriptAlertsPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(xpath = "//button[text()='Click for JS Alert']")
    private WebElement jsAlertButton;
    
    @FindBy(xpath = "//button[text()='Click for JS Confirm']")
    private WebElement jsConfirmButton;
    
    @FindBy(xpath = "//button[text()='Click for JS Prompt']")
    private WebElement jsPromptButton;
    
    @FindBy(id = "result")
    private WebElement result;
    
    public JavaScriptAlertsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");
    }
    
    public void clickJSAlert() {
        jsAlertButton.click();
    }
    
    public void clickJSConfirm() {
        jsConfirmButton.click();
    }
    
    public void clickJSPrompt() {
        jsPromptButton.click();
    }
    
    public void acceptAlert() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }
    
    public void dismissAlert() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.dismiss();
    }
    
    public void sendTextToAlert(String text) {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.sendKeys(text);
        alert.accept();
    }
    
    public String getAlertText() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        return alert.getText();
    }
    
    public String getResultText() {
        return result.getText();
    }
}