package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.interactions.Actions;
import io.qameta.allure.Step;
import java.util.List;

public class HoversPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(css = ".figure")
    private List<WebElement> figures;
    
    @FindBy(css = ".figcaption h5")
    private List<WebElement> captions;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    public HoversPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to hovers page")
    public HoversPage navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/hovers");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Hover over figure {index}")
    public HoversPage hoverOverFigure(int index) {
        if (index < figures.size()) {
            Actions actions = new Actions(driver);
            actions.moveToElement(figures.get(index)).perform();
        }
        return this;
    }
    
    @Step("Verify caption is displayed for figure {index}")
    public boolean isCaptionDisplayed(int index) {
        if (index < captions.size()) {
            return captions.get(index).isDisplayed();
        }
        return false;
    }
    
    @Step("Get caption text for figure {index}")
    public String getCaptionText(int index) {
        if (index < captions.size() && isCaptionDisplayed(index)) {
            return captions.get(index).getText();
        }
        return "";
    }
}