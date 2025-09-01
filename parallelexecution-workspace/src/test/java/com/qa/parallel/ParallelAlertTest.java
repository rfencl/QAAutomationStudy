package com.qa.parallel;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class ParallelAlertTest extends ParallelTestBase {
    
    @Test
    public void testSimpleAlert() {
        logTestInfo("testSimpleAlert");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");
        
        driver.findElement(By.xpath("//button[text()='Click for JS Alert']")).click();
        
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = alert.getText();
        Assert.assertEquals(alertText, "I am a JS Alert");
        alert.accept();
        
        String result = driver.findElement(By.id("result")).getText();
        Assert.assertEquals(result, "You successfully clicked an alert");
        
        System.out.printf("[Thread-%d] Simple alert test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testConfirmAlert() {
        logTestInfo("testConfirmAlert");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");
        
        driver.findElement(By.xpath("//button[text()='Click for JS Confirm']")).click();
        
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
        
        String result = driver.findElement(By.id("result")).getText();
        Assert.assertEquals(result, "You clicked: Ok");
        
        System.out.printf("[Thread-%d] Confirm alert test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testPromptAlert() {
        logTestInfo("testPromptAlert");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/javascript_alerts");
        
        driver.findElement(By.xpath("//button[text()='Click for JS Prompt']")).click();
        
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.sendKeys("Test Input");
        alert.accept();
        
        String result = driver.findElement(By.id("result")).getText();
        Assert.assertEquals(result, "You entered: Test Input");
        
        System.out.printf("[Thread-%d] Prompt alert test completed%n", Thread.currentThread().getId());
    }
}
