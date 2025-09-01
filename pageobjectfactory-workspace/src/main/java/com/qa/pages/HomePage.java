package com.qa.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class HomePage extends BasePage {
    
    @FindBy(linkText = "Form Authentication")
    private WebElement formAuthLink;
    
    @FindBy(linkText = "Checkboxes")
    private WebElement checkboxesLink;
    
    @FindBy(linkText = "Dropdown")
    private WebElement dropdownLink;
    
    @FindBy(css = "h1")
    private WebElement pageHeading;
    
    public HomePage(WebDriver driver) {
        super(driver);
    }
    
    public void clickFormAuthentication() {
        wait.until(ExpectedConditions.elementToBeClickable(formAuthLink));
        formAuthLink.click();
    }
    
    public void clickCheckboxes() {
        wait.until(ExpectedConditions.elementToBeClickable(checkboxesLink));
        checkboxesLink.click();
    }
    
    public void clickDropdown() {
        wait.until(ExpectedConditions.elementToBeClickable(dropdownLink));
        dropdownLink.click();
    }
    
    public String getPageHeading() {
        wait.until(ExpectedConditions.visibilityOf(pageHeading));
        return pageHeading.getText();
    }
}
