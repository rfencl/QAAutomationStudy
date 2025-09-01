package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.qameta.allure.Step;
import java.util.List;

public class CheckboxesPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(css = "input[type='checkbox']")
    private List<WebElement> checkboxes;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    public CheckboxesPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to checkboxes page")
    public CheckboxesPage navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/checkboxes");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Check checkbox at index {index}")
    public CheckboxesPage checkCheckbox(int index) {
        if (index < checkboxes.size() && !checkboxes.get(index).isSelected()) {
            checkboxes.get(index).click();
        }
        return this;
    }
    
    @Step("Uncheck checkbox at index {index}")
    public CheckboxesPage uncheckCheckbox(int index) {
        if (index < checkboxes.size() && checkboxes.get(index).isSelected()) {
            checkboxes.get(index).click();
        }
        return this;
    }
    
    @Step("Verify checkbox {index} is checked")
    public boolean isCheckboxChecked(int index) {
        return index < checkboxes.size() && checkboxes.get(index).isSelected();
    }
    
    @Step("Get number of checkboxes")
    public int getCheckboxCount() {
        return checkboxes.size();
    }
}