package com.qa.selenium.pages;

import com.qa.selenium.base.BaseTest;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.qameta.allure.Step;

/**
 * Page Object Model for Login Page
 * Exercise: Implement a Page Object Model for a sample website
 * Exercise: Automate a login form with valid and invalid data
 */
public class LoginPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    // Page elements using @FindBy annotations
    @FindBy(id = "username")
    private WebElement usernameField;
    
    @FindBy(id = "password")
    private WebElement passwordField;
    
    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;
    
    @FindBy(css = ".flash.success")
    private WebElement successMessage;
    
    @FindBy(css = ".flash.error")
    private WebElement errorMessage;
    
    @FindBy(linkText = "Logout")
    private WebElement logoutLink;
    
    @FindBy(css = "h2")
    private WebElement pageTitle;
    
    @FindBy(css = ".subheader")
    private WebElement pageSubtitle;
    
    // Constructor
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = BaseTest.getWait();
        PageFactory.initElements(driver, this);
    }
    
    /**
     * Navigate to login page
     */
    @Step("Navigate to login page")
    public LoginPage navigateToLoginPage() {
        driver.get("https://the-internet.herokuapp.com/login");
        waitForPageToLoad();
        return this;
    }
    
    /**
     * Wait for page to load
     */
    @Step("Wait for login page to load")
    public LoginPage waitForPageToLoad() {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        return this;
    }
    
    /**
     * Enter username
     */
    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        wait.until(ExpectedConditions.visibilityOf(usernameField));
        usernameField.clear();
        usernameField.sendKeys(username);
        return this;
    }
    
    /**
     * Enter password
     */
    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        wait.until(ExpectedConditions.visibilityOf(passwordField));
        passwordField.clear();
        passwordField.sendKeys(password);
        return this;
    }
    
    /**
     * Click login button
     */
    @Step("Click login button")
    public LoginPage clickLoginButton() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton));
        loginButton.click();
        return this;
    }
    
    /**
     * Perform login with credentials
     */
    @Step("Login with username: {username}")
    public LoginPage login(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        clickLoginButton();
        return this;
    }
    
    /**
     * Check if login was successful
     */
    @Step("Verify login success")
    public boolean isLoginSuccessful() {
        try {
            wait.until(ExpectedConditions.visibilityOf(successMessage));
            return successMessage.isDisplayed() && 
                   successMessage.getText().contains("You logged into a secure area!");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if login failed
     */
    @Step("Verify login failure")
    public boolean isLoginFailed() {
        try {
            wait.until(ExpectedConditions.visibilityOf(errorMessage));
            return errorMessage.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get error message text
     */
    @Step("Get error message")
    public String getErrorMessage() {
        if (isLoginFailed()) {
            return errorMessage.getText();
        }
        return "";
    }
    
    /**
     * Get success message text
     */
    @Step("Get success message")
    public String getSuccessMessage() {
        if (isLoginSuccessful()) {
            return successMessage.getText();
        }
        return "";
    }
    
    /**
     * Check if logout link is present (indicates successful login)
     */
    @Step("Verify logout link is present")
    public boolean isLogoutLinkPresent() {
        try {
            return logoutLink.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Perform logout
     */
    @Step("Logout")
    public LoginPage logout() {
        if (isLogoutLinkPresent()) {
            logoutLink.click();
            waitForPageToLoad();
        }
        return this;
    }
    
    /**
     * Get page title
     */
    @Step("Get page title")
    public String getPageTitle() {
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return pageTitle.getText();
    }
    
    /**
     * Get page subtitle
     */
    @Step("Get page subtitle")
    public String getPageSubtitle() {
        wait.until(ExpectedConditions.visibilityOf(pageSubtitle));
        return pageSubtitle.getText();
    }
    
    /**
     * Check if username field is enabled
     */
    @Step("Verify username field is enabled")
    public boolean isUsernameFieldEnabled() {
        return usernameField.isEnabled();
    }
    
    /**
     * Check if password field is enabled
     */
    @Step("Verify password field is enabled")
    public boolean isPasswordFieldEnabled() {
        return passwordField.isEnabled();
    }
    
    /**
     * Check if login button is enabled
     */
    @Step("Verify login button is enabled")
    public boolean isLoginButtonEnabled() {
        return loginButton.isEnabled();
    }
    
    /**
     * Get username field placeholder
     */
    @Step("Get username field placeholder")
    public String getUsernameFieldPlaceholder() {
        return usernameField.getAttribute("placeholder");
    }
    
    /**
     * Get password field placeholder
     */
    @Step("Get password field placeholder")
    public String getPasswordFieldPlaceholder() {
        return passwordField.getAttribute("placeholder");
    }
    
    /**
     * Clear all fields
     */
    @Step("Clear all fields")
    public LoginPage clearAllFields() {
        usernameField.clear();
        passwordField.clear();
        return this;
    }
    
    /**
     * Get current username value
     */
    @Step("Get current username value")
    public String getCurrentUsername() {
        return usernameField.getAttribute("value");
    }
    
    /**
     * Get current password value
     */
    @Step("Get current password value")
    public String getCurrentPassword() {
        return passwordField.getAttribute("value");
    }
    
    /**
     * Validate page elements are present
     */
    @Step("Validate all page elements are present")
    public boolean validatePageElements() {
        try {
            return usernameField.isDisplayed() &&
                   passwordField.isDisplayed() &&
                   loginButton.isDisplayed() &&
                   pageTitle.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get login button text
     */
    @Step("Get login button text")
    public String getLoginButtonText() {
        return loginButton.getText();
    }
}
