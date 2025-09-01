package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.interactions.Actions;
import io.qameta.allure.Step;

public class DragAndDropPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(id = "column-a")
    private WebElement columnA;
    
    @FindBy(id = "column-b")
    private WebElement columnB;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    public DragAndDropPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to drag and drop page")
    public DragAndDropPage navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/drag_and_drop");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Drag column A to column B")
    public DragAndDropPage dragAToB() {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(columnA, columnB).perform();
        return this;
    }
    
    @Step("Get column A text")
    public String getColumnAText() {
        return columnA.getText();
    }
    
    @Step("Get column B text")
    public String getColumnBText() {
        return columnB.getText();
    }
}