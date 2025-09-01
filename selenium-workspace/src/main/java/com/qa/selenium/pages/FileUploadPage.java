package com.qa.selenium.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class FileUploadPage {
    private WebDriver driver;
    private WebDriverWait wait;
    
    @FindBy(id = "file-upload")
    private WebElement fileInput;
    
    @FindBy(id = "file-submit")
    private WebElement uploadButton;
    
    @FindBy(id = "uploaded-files")
    private WebElement uploadedFiles;
    
    public FileUploadPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }
    
    public void navigateToPage() {
        driver.get("https://the-internet.herokuapp.com/upload");
    }
    
    public void selectFile(String filePath) {
        fileInput.sendKeys(filePath);
    }
    
    public void clickUpload() {
        uploadButton.click();
    }
    
    public String getUploadedFileName() {
        return uploadedFiles.getText();
    }
    
    public boolean isFileUploaded() {
        return !uploadedFiles.getText().isEmpty();
    }
}