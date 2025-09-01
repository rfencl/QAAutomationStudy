package com.qa.parallel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class ParallelNavigationTest extends ParallelTestBase {
    
    @Test
    public void testPageNavigation() {
        logTestInfo("testPageNavigation");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/");
        
        String initialTitle = driver.getTitle();
        Assert.assertEquals(initialTitle, "The Internet");
        
        driver.findElement(By.linkText("A/B Testing")).click();
        wait.until(ExpectedConditions.titleContains("A/B Test"));
        
        String newTitle = driver.getTitle();
        Assert.assertTrue(newTitle.contains("A/B Test"));
        
        System.out.printf("[Thread-%d] Navigation test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testBackNavigation() {
        logTestInfo("testBackNavigation");
        WebDriver driver = getDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        
        driver.get("https://the-internet.herokuapp.com/");
        String initialUrl = driver.getCurrentUrl();
        
        driver.findElement(By.linkText("Basic Auth")).click();
        wait.until(ExpectedConditions.urlContains("basic_auth"));
        
        driver.navigate().back();
        wait.until(ExpectedConditions.urlToBe(initialUrl));
        
        Assert.assertEquals(driver.getCurrentUrl(), initialUrl);
        
        System.out.printf("[Thread-%d] Back navigation test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testRefreshPage() {
        logTestInfo("testRefreshPage");
        WebDriver driver = getDriver();
        
        driver.get("https://the-internet.herokuapp.com/");
        String initialTitle = driver.getTitle();
        
        driver.navigate().refresh();
        
        String refreshedTitle = driver.getTitle();
        Assert.assertEquals(refreshedTitle, initialTitle);
        
        System.out.printf("[Thread-%d] Refresh test completed%n", Thread.currentThread().getId());
    }
}
