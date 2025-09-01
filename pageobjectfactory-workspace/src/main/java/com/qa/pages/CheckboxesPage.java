package com.qa.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

public class CheckboxesPage extends BasePage {
    
    @FindBy(css = "input[type='checkbox']")
    private List<WebElement> checkboxes;
    
    @FindBy(css = "h3")
    private WebElement pageHeading;
    
    public CheckboxesPage(WebDriver driver) {
        super(driver);
    }
    
    public void clickCheckbox(int index) {
        wait.until(ExpectedConditions.visibilityOfAllElements(checkboxes));
        if (index >= 0 && index < checkboxes.size()) {
            checkboxes.get(index).click();
        }
    }
    
    public boolean isCheckboxSelected(int index) {
        if (index >= 0 && index < checkboxes.size()) {
            return checkboxes.get(index).isSelected();
        }
        return false;
    }
    
    public int getCheckboxCount() {
        wait.until(ExpectedConditions.visibilityOfAllElements(checkboxes));
        return checkboxes.size();
    }
    
    public String getPageHeading() {
        wait.until(ExpectedConditions.visibilityOf(pageHeading));
        return pageHeading.getText();
    }
}
