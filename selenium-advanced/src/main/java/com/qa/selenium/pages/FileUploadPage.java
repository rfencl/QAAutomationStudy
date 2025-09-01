package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import io.qameta.allure.Step;

public class FileUploadPage {
    
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(id = "file-upload")
    private WebElement fileInput;
    
    @FindBy(id = "file-submit")
    private WebElement uploadButton;
    
    @FindBy(css = "h3")
    private WebElement pageTitle;
    
    @FindBy(id = "uploaded-files")
    private WebElement uploadedFiles;
    
    public FileUploadPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(20));
        PageFactory.initElements(driver, this);
    }
    
    @Step("Navigate to file upload page")
    public FileUploadPage navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/upload");
        wait.until(ExpectedConditions.visibilityOf(pageTitle));
        return this;
    }
    
    @Step("Select file: {filePath}")
    public FileUploadPage selectFile(String filePath) {
        fileInput.sendKeys(filePath);
        return this;
    }
    
    @Step("Click upload button")
    public FileUploadPage clickUpload() {
        uploadButton.click();
        return this;
    }
    
    @Step("Verify file uploaded successfully")
    public boolean isFileUploaded() {
        try {
            wait.until(ExpectedConditions.visibilityOf(uploadedFiles));
            return uploadedFiles.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
    
    @Step("Get uploaded file name")
    public String getUploadedFileName() {
        if (isFileUploaded()) {
            return uploadedFiles.getText();
        }
        return "";
    }
}