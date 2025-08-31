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
 * Page Object Model (POM) implementation for Login Page
 */
public class LoginPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    // Using @FindBy annotations (Page Factory pattern)
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(xpath = "//button[@type='submit']")
    private WebElement loginButton;
    
    @FindBy(xpath = "//div[contains(@class,'flash error')]")
    private WebElement errorMessage;
    
    @FindBy(xpath = "//div[@class='flash-success']")
    private WebElement successMessage;
    
    // Traditional locators (without Page Factory)
    private By username = By.id("username");
    private By password = By.id("password");
    private By loginBtn = By.xpath("//button[@type='submit']");
    private By errorMsg = By.xpath("//div[contains(@class,'flash error')]");
    private By successMsg = By.xpath("//div[@class='flash-success']");
    
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
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
        
        // Wait for login button to be clickable then click
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginBtn));
        loginButton.click();
        
        // Wait a moment for the page to process the login
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public String getErrorMessage() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
            return element.getText();
        } catch (Exception e) {
            return "";
        }
    }
    
    public boolean isErrorMessageDisplayed() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(errorMsg));
            return element.isDisplayed();
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
    
    public boolean isLoginSuccessful() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'flash success')]"))
            );
            return element.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getSuccessMessage() {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//div[contains(@class,'flash success')]"))
            );
            return element.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
