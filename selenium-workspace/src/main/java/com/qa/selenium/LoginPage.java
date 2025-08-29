package com.qa.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * Page Object Model (POM) implementation for Login Page
 */
public class LoginPage {
    private WebDriver driver;
    
    // Using @FindBy annotations (Page Factory pattern)
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(id = "login")
    private WebElement loginButton;
    
    @FindBy(xpath = "//div[@class='error-message']")
    private WebElement errorMessage;
    
    // Traditional locators (without Page Factory)
    private By username = By.id("username");
    private By password = By.id("password");
    private By loginBtn = By.id("login");
    private By errorMsg = By.xpath("//div[@class='error-message']");
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }
    
    // Method using Page Factory elements
    public void loginWithPageFactory(String user, String pass) {
        usernameField.clear();
        usernameField.sendKeys(user);
        passwordField.clear();
        passwordField.sendKeys(pass);
        loginButton.click();
    }
    
    // Method using traditional locators
    public void login(String user, String pass) {
        driver.findElement(username).clear();
        driver.findElement(username).sendKeys(user);
        driver.findElement(password).clear();
        driver.findElement(password).sendKeys(pass);
        driver.findElement(loginBtn).click();
    }
    
    public String getErrorMessage() {
        return driver.findElement(errorMsg).getText();
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            return driver.findElement(errorMsg).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public void clearFields() {
        driver.findElement(username).clear();
        driver.findElement(password).clear();
    }
    
    public String getPageTitle() {
        return driver.getTitle();
    }
}
