package com.qa.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class DropdownPage extends BasePage {
    
    @FindBy(id = "dropdown")
    private WebElement dropdownElement;
    
    @FindBy(css = "h3")
    private WebElement pageHeading;
    
    public DropdownPage(WebDriver driver) {
        super(driver);
    }
    
    public void selectByValue(String value) {
        wait.until(ExpectedConditions.visibilityOf(dropdownElement));
        Select dropdown = new Select(dropdownElement);
        dropdown.selectByValue(value);
    }
    
    public void selectByText(String text) {
        wait.until(ExpectedConditions.visibilityOf(dropdownElement));
        Select dropdown = new Select(dropdownElement);
        dropdown.selectByVisibleText(text);
    }
    
    public String getSelectedOption() {
        Select dropdown = new Select(dropdownElement);
        return dropdown.getFirstSelectedOption().getText();
    }
    
    public String getPageHeading() {
        wait.until(ExpectedConditions.visibilityOf(pageHeading));
        return pageHeading.getText();
    }
}
