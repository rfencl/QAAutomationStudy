package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class CheckboxesPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(xpath = "//input[@type='checkbox']")
    private List<WebElement> checkboxes;
    
    public CheckboxesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/checkboxes");
    }
    
    public void clickCheckbox(int index) {
        checkboxes.get(index).click();
    }
    
    public boolean isCheckboxSelected(int index) {
        return checkboxes.get(index).isSelected();
    }
    
    public int getCheckboxCount() {
        return checkboxes.size();
    }
}