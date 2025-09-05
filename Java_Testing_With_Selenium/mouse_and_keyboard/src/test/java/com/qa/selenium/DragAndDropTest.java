package com.qa.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;
import org.testng.Assert;

public class DragAndDropTest {
    @Test(priority = 1)
    public void testDragAndDrop() throws InterruptedException {
        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver();
        SeleniumHelper seleniumHelper = new SeleniumHelper(driver);
        // Navigate to the location of the HTML file
        driver.get(
                "file:///home/rick/Documents/QAAutomationStudy/Java_Testing_With_Selenium/mouse_and_keyboard/src/test/pages/dragndrop.html");
        // Replace with the actual path to your HTML file
        // Locate the source (circle) and target
        seleniumHelper.waitForElementVisible(By.id("drag"));
        WebElement sourceElement = driver.findElement(By.id("drag"));
        WebElement targetElement = driver.findElement(By.id("drop"));
        // Create an instance of the Actions class
        Actions actions = new Actions(driver);
        // Perform the drag-and-drop action
        actions.dragAndDrop(sourceElement, targetElement).perform();
        Assert.assertEquals(sourceElement.getText(), "Dropped!", "Element not dropped as expected");
        Assert.assertEquals(sourceElement.getAttribute("style"),
                "background-color: green;", "Element not dropped as expected");
        // Optionally, close the browser
        driver.quit();
    }

    @Test(priority = 2)
    public void testDragAndDropAborted() throws InterruptedException {
        // Initialize the WebDriver
        WebDriver driver = new ChromeDriver();
        SeleniumHelper seleniumHelper = new SeleniumHelper(driver);
        // Navigate to the location of the HTML file
        // driver.get("file://src/test/pages/dragndrop.html");
        driver.get(
                "file:///home/rick/Documents/QAAutomationStudy/Java_Testing_With_Selenium/mouse_and_keyboard/src/test/pages/dragndrop2.html");
        // Replace with the actual path to your HTML file
        // Locate the source (circle) and target
        seleniumHelper.waitForElementVisible(By.id("drag"));
        WebElement sourceElement = driver.findElement(By.id("drag"));
        // Create an instance of the Actions class
        Actions actions = new Actions(driver);
        // Perform the drag-and-drop action
        actions.dragAndDropBy(sourceElement, 100, 100).perform();
        Assert.assertEquals(sourceElement.getText(), "drag", "Element not dropped as expected");
        Assert.assertEquals(sourceElement.getAttribute("style"),
                "background-color: rgb(169, 169, 169);", "Element not dropped as expected");
        // Optionally, close the browser
        driver.quit();
    }
}
