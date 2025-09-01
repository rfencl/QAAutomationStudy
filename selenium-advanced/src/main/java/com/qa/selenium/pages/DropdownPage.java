package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import io.qameta.allure.Step;

public class DropdownPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(id = "dropdown")
    private WebElement dropdown;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    public DropdownPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to dropdown page")
    public DropdownPage navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/dropdown");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Select option by value: {value}")
    public DropdownPage selectByValue(String value) {
        Select select = new Select(dropdown);
        select.selectByValue(value);
        return this;
    }
    
    @Step("Select option by text: {text}")
    public DropdownPage selectByText(String text) {
        Select select = new Select(dropdown);
        select.selectByVisibleText(text);
        return this;
    }
    
    @Step("Get selected option text")
    public String getSelectedOptionText() {
        Select select = new Select(dropdown);
        return select.getFirstSelectedOption().getText();
    }
    
    @Step("Get selected option value")
    public String getSelectedOptionValue() {
        Select select = new Select(dropdown);
        return select.getFirstSelectedOption().getAttribute("value");
    }
}