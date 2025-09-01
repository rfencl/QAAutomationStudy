package com.qa.selenium.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class DynamicControlsPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(xpath = "//button[text()='Remove']")
    private WebElement removeButton;
    
    @FindBy(xpath = "//button[text()='Add']")
    private WebElement addButton;
    
    @FindBy(xpath = "//button[text()='Enable']")
    private WebElement enableButton;
    
    @FindBy(xpath = "//button[text()='Disable']")
    private WebElement disableButton;
    
    @FindBy(xpath = "//input[@type='checkbox']")
    private WebElement checkbox;
    
    @FindBy(xpath = "//input[@type='text']")
    private WebElement textInput;
    
    public DynamicControlsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/dynamic_controls");
    }
    
    public void clickRemove() {
        removeButton.click();
    }
    
    public void clickAdd() {
        addButton.click();
    }
    
    public void clickEnable() {
        enableButton.click();
    }
    
    public void clickDisable() {
        disableButton.click();
    }
    
    public boolean isCheckboxPresent() {
        try {
            return checkbox.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isTextInputEnabled() {
        return textInput.isEnabled();
    }
    
    public void waitForCheckboxToDisappear() {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//input[@type='checkbox']")));
    }
    
    public void waitForCheckboxToAppear() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@type='checkbox']")));
    }
}