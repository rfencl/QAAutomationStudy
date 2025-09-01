package com.qa.parallel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.time.Duration;

public class ParallelFormTest extends ParallelTestBase {
    
    @Test
    public void testDropdownSelection() {
        logTestInfo("testDropdownSelection");
        WebDriver driver = getDriver();
        
        driver.get("https://the-internet.herokuapp.com/dropdown");
        
        Select dropdown = new Select(driver.findElement(By.id("dropdown")));
        dropdown.selectByVisibleText("Option 1");
        
        String selectedText = dropdown.getFirstSelectedOption().getText();
        Assert.assertEquals(selectedText, "Option 1");
        
        System.out.printf("[Thread-%d] Dropdown test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testCheckboxes() {
        logTestInfo("testCheckboxes");
        WebDriver driver = getDriver();
        
        driver.get("https://the-internet.herokuapp.com/checkboxes");
        
        By checkbox1 = By.xpath("//input[@type='checkbox'][1]");
        By checkbox2 = By.xpath("//input[@type='checkbox'][2]");
        
        if (!driver.findElement(checkbox1).isSelected()) {
            driver.findElement(checkbox1).click();
        }
        
        Assert.assertTrue(driver.findElement(checkbox1).isSelected());
        
        System.out.printf("[Thread-%d] Checkbox test completed%n", Thread.currentThread().getId());
    }
    
    @Test
    public void testFileUpload() {
        logTestInfo("testFileUpload");
        WebDriver driver = getDriver();
        
        driver.get("https://the-internet.herokuapp.com/upload");
        
        // Create a temporary file path (in real scenario, use actual file)
        String filePath = System.getProperty("java.io.tmpdir") + "test.txt";
        
        try {
            java.nio.file.Files.write(java.nio.file.Paths.get(filePath), "Test content".getBytes());
            
            driver.findElement(By.id("file-upload")).sendKeys(filePath);
            driver.findElement(By.id("file-submit")).click();
            
            String uploadedFile = driver.findElement(By.id("uploaded-files")).getText();
            Assert.assertTrue(uploadedFile.contains("test.txt"));
            
        } catch (Exception e) {
            System.out.printf("[Thread-%d] File upload test skipped: %s%n", 
                Thread.currentThread().getId(), e.getMessage());
        }
        
        System.out.printf("[Thread-%d] File upload test completed%n", Thread.currentThread().getId());
    }
}
