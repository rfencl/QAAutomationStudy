package com.qa.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends BasePage {
    
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;
    
    @FindBy(id = "flash")
    private WebElement flashMessage;
    
    public LoginPage(WebDriver driver) {
        super(driver);
    }
    
    public LoginPage enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        return this;
    }
    
    public LoginPage enterPassword(String password) {
        passwordField.clear();
        passwordField.sendKeys(password);
        return this;
    }
    
    public void clickLogin() {
        loginButton.click();
    }
    
    public String getFlashMessage() {
        wait.until(ExpectedConditions.visibilityOf(flashMessage));
        return flashMessage.getText();
    }
    
    public boolean isLoginSuccessful() {
        return getCurrentUrl().contains("/secure");
    }
}
