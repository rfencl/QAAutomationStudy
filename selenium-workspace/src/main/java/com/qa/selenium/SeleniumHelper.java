package com.qa.selenium;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.FluentWait;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Reusable helper methods for Selenium operations
 */
public class SeleniumHelper {
    private WebDriver driver;
    private WebDriverWait wait;
    
    public SeleniumHelper(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    // Reusable click method
    public void clickElement(By locator) {
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
    }
    
    // Explicit wait examples
    public WebElement waitForElementVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    public WebElement waitForElementClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    public boolean waitForElementInvisible(By locator) {
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
    
    // Fluent wait example
    public WebElement fluentWait(By locator) {
        FluentWait<WebDriver> fluentWait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(30))
                .pollingEvery(Duration.ofSeconds(2))
                .ignoring(NoSuchElementException.class);
        
        return fluentWait.until(driver -> driver.findElement(locator));
    }
    
    // findElement vs findElements demonstration
    public void demonstrateFindMethods(By locator) {
        try {
            // findElement - throws exception if not found
            WebElement singleElement = driver.findElement(locator);
            System.out.println("Single element found: " + singleElement.getTagName());
        } catch (NoSuchElementException e) {
            System.out.println("Element not found with findElement");
        }
        
        // findElements - returns empty list if not found
        List<WebElement> elements = driver.findElements(locator);
        System.out.println("Number of elements found: " + elements.size());
    }
    
    // Alert handling
    public void handleAlert(String action) {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            System.out.println("Alert text: " + alertText);
            
            switch (action.toLowerCase()) {
                case "accept":
                    alert.accept();
                    break;
                case "dismiss":
                    alert.dismiss();
                    break;
                default:
                    System.out.println("Invalid action. Use 'accept' or 'dismiss'");
            }
        } catch (NoAlertPresentException e) {
            System.out.println("No alert present");
        }
    }
    
    // Dropdown handling
    public void selectFromDropdown(By locator, String selectionType, String value) {
        Select select = new Select(driver.findElement(locator));
        
        switch (selectionType.toLowerCase()) {
            case "text":
                select.selectByVisibleText(value);
                break;
            case "value":
                select.selectByValue(value);
                break;
            case "index":
                select.selectByIndex(Integer.parseInt(value));
                break;
            default:
                System.out.println("Invalid selection type. Use 'text', 'value', or 'index'");
        }
    }
    
    // Multiple windows handling
    public void switchToWindow(String windowTitle) {
        String parentWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        
        for (String windowHandle : allWindows) {
            driver.switchTo().window(windowHandle);
            if (driver.getTitle().contains(windowTitle)) {
                System.out.println("Switched to window: " + windowTitle);
                return;
            }
        }
        
        // If window not found, switch back to parent
        driver.switchTo().window(parentWindow);
        System.out.println("Window not found, switched back to parent");
    }
    
    // Dynamic element handling with partial matching
    public WebElement findDynamicElement(String partialId) {
        By dynamicLocator = By.xpath("//button[contains(@id, '" + partialId + "')]");
        return waitForElementVisible(dynamicLocator);
    }
    
    // XPath vs CSS Selector examples
    public void demonstrateLocatorStrategies() {
        // XPath examples
        By xpathById = By.xpath("//input[@id='username']");
        By xpathByText = By.xpath("//button[text()='Login']");
        By xpathByContains = By.xpath("//div[contains(@class, 'error')]");
        By xpathByParent = By.xpath("//input[@id='username']/parent::div");
        
        // CSS Selector examples
        By cssById = By.cssSelector("#username");
        By cssByClass = By.cssSelector(".error-message");
        By cssByAttribute = By.cssSelector("input[type='password']");
        By cssByParent = By.cssSelector("div > input#username");
        
        System.out.println("Locator strategies demonstrated");
    }
}
