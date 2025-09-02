package dev.selenium.getting_started; // Define package namespace

import org.openqa.selenium.By; // Import locator strategies
import org.openqa.selenium.WebDriver; // Import WebDriver interface
import org.openqa.selenium.WebElement; // Import WebElement interface
import org.openqa.selenium.chrome.ChromeDriver; // Import Chrome browser driver

import java.time.Duration; // Import Duration class for timeouts

/**
 * https://www.selenium.dev/documentation/webdriver/getting_started/first_script/
 */
public class FirstScript {
    public static void main(String[] args) { // Main method entry point
        // Start the session
        WebDriver driver = new ChromeDriver(); // Create new Chrome browser instance

        // Take action on browser
        driver.get("https://www.selenium.dev/selenium/web/web-form.html"); // Navigate to test page

        String title = driver.getTitle(); // Get page title
        System.out.println("Page title: " + title);

        System.out.println("Current URL: " + driver.getCurrentUrl());

        driver.manage().timeouts().implicitlyWait(Duration.ofMillis(500)); // Set implicit wait timeout

        WebElement textBox = driver.findElement(By.name("my-text")); // Find text input by name attribute
        WebElement submitButton = driver.findElement(By.cssSelector("button")); // Find submit button by CSS selector

        textBox.sendKeys("Selenium"); // Type "Selenium" into text box
        submitButton.click(); // Click the submit button

        WebElement message = driver.findElement(By.id("message")); // Find success message by ID
        String messageText = message.getText(); // Get message text
        System.out.println("Success message: " + messageText);

        driver.quit(); // Close browser and end session
    }
}
