package com.qa.selenium.utils;

import com.qa.selenium.base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.interactions.Actions;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Utility class for common WebDriver operations
 * Handles waits, element interactions, and advanced scenarios
 */
public class WebDriverUtils {
    
    private static WebDriver driver;
    private static WebDriverWait wait;
    
    static {
        driver = BaseTest.getDriver();
        wait = BaseTest.getWait();
    }
    
    /**
     * Explicit wait for element to be visible
     */
    public static WebElement waitForElementVisible(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    /**
     * Explicit wait for element to be clickable
     */
    public static WebElement waitForElementClickable(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    /**
     * Fluent wait for element with custom polling
     */
    public static WebElement fluentWaitForElement(By locator, int timeoutSeconds, int pollingSeconds) {
        org.openqa.selenium.support.ui.FluentWait<WebDriver> fluentWait = 
            new org.openqa.selenium.support.ui.FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSeconds))
                .pollingEvery(Duration.ofSeconds(pollingSeconds))
                .ignoring(NoSuchElementException.class);
        
        return fluentWait.until(driver -> driver.findElement(locator));
    }
    
    /**
     * Wait for element to disappear
     */
    public static boolean waitForElementToDisappear(By locator, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            return customWait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            return false;
        }
    }
    
    /**
     * Safe click with retry mechanism
     */
    public static void safeClick(WebElement element, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                wait.until(ExpectedConditions.elementToBeClickable(element));
                element.click();
                return;
            } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                if (i == maxRetries - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Safe send keys with retry mechanism
     */
    public static void safeSendKeys(WebElement element, String text, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                wait.until(ExpectedConditions.visibilityOf(element));
                element.clear();
                element.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (i == maxRetries - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Handle dropdown selection by visible text
     */
    public static void selectDropdownByText(WebElement dropdown, String text) {
        Select select = new Select(dropdown);
        select.selectByVisibleText(text);
    }
    
    /**
     * Handle dropdown selection by value
     */
    public static void selectDropdownByValue(WebElement dropdown, String value) {
        Select select = new Select(dropdown);
        select.selectByValue(value);
    }
    
    /**
     * Handle dropdown selection by index
     */
    public static void selectDropdownByIndex(WebElement dropdown, int index) {
        Select select = new Select(dropdown);
        select.selectByIndex(index);
    }
    
    /**
     * Get all dropdown options
     */
    public static List<WebElement> getDropdownOptions(WebElement dropdown) {
        Select select = new Select(dropdown);
        return select.getOptions();
    }
    
    /**
     * Handle JavaScript alerts
     */
    public static void handleAlert(boolean accept) {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            if (accept) {
                alert.accept();
            } else {
                alert.dismiss();
            }
        } catch (TimeoutException e) {
            System.out.println("No alert present");
        }
    }
    
    /**
     * Handle JavaScript alerts with text input
     */
    public static void handlePromptAlert(String text, boolean accept) {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            if (text != null && !text.isEmpty()) {
                alert.sendKeys(text);
            }
            if (accept) {
                alert.accept();
            } else {
                alert.dismiss();
            }
        } catch (TimeoutException e) {
            System.out.println("No alert present");
        }
    }
    
    /**
     * Get alert text
     */
    public static String getAlertText() {
        try {
            Alert alert = wait.until(ExpectedConditions.alertIsPresent());
            return alert.getText();
        } catch (TimeoutException e) {
            return "";
        }
    }
    
    /**
     * Switch to frame by index
     */
    public static void switchToFrame(int index) {
        driver.switchTo().frame(index);
    }
    
    /**
     * Switch to frame by name or id
     */
    public static void switchToFrame(String nameOrId) {
        driver.switchTo().frame(nameOrId);
    }
    
    /**
     * Switch to frame by WebElement
     */
    public static void switchToFrame(WebElement frameElement) {
        driver.switchTo().frame(frameElement);
    }
    
    /**
     * Switch to default content (exit frame)
     */
    public static void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }
    
    /**
     * Handle multiple windows
     */
    public static void switchToWindow(String windowHandle) {
        driver.switchTo().window(windowHandle);
    }
    
    /**
     * Get all window handles
     */
    public static Set<String> getAllWindowHandles() {
        return driver.getWindowHandles();
    }
    
    /**
     * Switch to new window (latest opened)
     */
    public static String switchToNewWindow() {
        String originalWindow = driver.getWindowHandle();
        Set<String> allWindows = driver.getWindowHandles();
        
        for (String window : allWindows) {
            if (!window.equals(originalWindow)) {
                driver.switchTo().window(window);
                return originalWindow;
            }
        }
        return originalWindow;
    }
    
    /**
     * Close current window and switch back
     */
    public static void closeCurrentWindowAndSwitchBack(String originalWindow) {
        driver.close();
        driver.switchTo().window(originalWindow);
    }
    
    /**
     * Hover over element
     */
    public static void hoverOverElement(WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element).perform();
    }
    
    /**
     * Double click on element
     */
    public static void doubleClick(WebElement element) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element).perform();
    }
    
    /**
     * Right click on element
     */
    public static void rightClick(WebElement element) {
        Actions actions = new Actions(driver);
        actions.contextClick(element).perform();
    }
    
    /**
     * Drag and drop
     */
    public static void dragAndDrop(WebElement source, WebElement target) {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(source, target).perform();
    }
    
    /**
     * Scroll to element using JavaScript
     */
    public static void scrollToElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }
    
    /**
     * Click element using JavaScript
     */
    public static void clickUsingJavaScript(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].click();", element);
    }
    
    /**
     * Set value using JavaScript
     */
    public static void setValueUsingJavaScript(WebElement element, String value) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].value='" + value + "';", element);
    }
    
    /**
     * Take screenshot
     */
    public static String takeScreenshot(String fileName) {
        try {
            TakesScreenshot screenshot = (TakesScreenshot) driver;
            File sourceFile = screenshot.getScreenshotAs(OutputType.FILE);
            String destinationPath = "screenshots/" + fileName + "_" + System.currentTimeMillis() + ".png";
            File destinationFile = new File(destinationPath);
            FileUtils.copyFile(sourceFile, destinationFile);
            return destinationPath;
        } catch (IOException e) {
            System.out.println("Failed to take screenshot: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Take element screenshot
     */
    public static String takeElementScreenshot(WebElement element, String fileName) {
        try {
            File sourceFile = element.getScreenshotAs(OutputType.FILE);
            String destinationPath = "screenshots/" + fileName + "_element_" + System.currentTimeMillis() + ".png";
            File destinationFile = new File(destinationPath);
            FileUtils.copyFile(sourceFile, destinationFile);
            return destinationPath;
        } catch (IOException e) {
            System.out.println("Failed to take element screenshot: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Wait for page to load completely
     */
    public static void waitForPageLoad() {
        wait.until(driver -> 
            ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }
    
    /**
     * Wait for jQuery to complete (if jQuery is present)
     */
    public static void waitForJQuery() {
        wait.until(driver -> 
            ((JavascriptExecutor) driver).executeScript("return jQuery.active == 0"));
    }
    
    /**
     * Check if element is present
     */
    public static boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    /**
     * Check if element is visible
     */
    public static boolean isElementVisible(WebElement element) {
        try {
            return element.isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }
    
    /**
     * Get element text with retry
     */
    public static String getElementText(WebElement element, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return element.getText();
            } catch (StaleElementReferenceException e) {
                if (i == maxRetries - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return "";
    }
    
    /**
     * Highlight element for debugging
     */
    public static void highlightElement(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border='3px solid red'", element);
    }
    
    /**
     * Remove highlight from element
     */
    public static void removeHighlight(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("arguments[0].style.border=''", element);
    }
}
