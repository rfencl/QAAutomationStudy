package com.qa.parallel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class ParallelLoginTest extends ParallelTestBase {
    
    @Test
    public void testValidLogin() {
        logTestInfo("testValidLogin");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/login");
        
        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password")).sendKeys("SuperSecretPassword!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.success")));
        String successMessage = driver.findElement(By.cssSelector(".flash.success")).getText();
        Assert.assertTrue(successMessage.contains("You logged into a secure area!"));
        
        System.out.printf("[Thread-%d] Login test completed successfully%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testInvalidLogin() {
        logTestInfo("testInvalidLogin");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/login");
        
        driver.findElement(By.id("username")).sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("invalid");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.error")));
        String errorMessage = driver.findElement(By.cssSelector(".flash.error")).getText();
        Assert.assertTrue(errorMessage.contains("Your username is invalid!"));
        
        System.out.printf("[Thread-%d] Invalid login test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testEmptyCredentials() {
        logTestInfo("testEmptyCredentials");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/login");
        
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.error")));
        String errorMessage = driver.findElement(By.cssSelector(".flash.error")).getText();
        Assert.assertTrue(errorMessage.contains("Your username is invalid!"));
        
        System.out.printf("[Thread-%d] Empty credentials test completed%n", Thread.currentThread().getId());
    }
}
