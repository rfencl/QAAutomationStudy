package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class DropdownPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(id = "dropdown")
    private WebElement dropdown;
    
    public DropdownPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/dropdown");
    }
    
    public void selectByValue(String value) {
        Select select = new Select(dropdown);
        select.selectByValue(value);
    }
    
    public void selectByText(String text) {
        Select select = new Select(dropdown);
        select.selectByVisibleText(text);
    }
    
    public String getSelectedOption() {
        Select select = new Select(dropdown);
        return select.getFirstSelectedOption().getText();
    }
}