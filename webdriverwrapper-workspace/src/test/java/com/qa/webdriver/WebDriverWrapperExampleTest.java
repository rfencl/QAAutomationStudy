package com.qa.webdriver;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WebDriverWrapperExampleTest {
    private WebDriverWrapper wrapper;
    
    @BeforeMethod
    public void setUp() {
        wrapper = WebDriverFactory.createWrapper("chrome", true);
    }
    
    @AfterMethod
    public void tearDown() {
        if (wrapper != null) {
            wrapper.quit();
        }
    }
    
    @Test
    public void testCompleteLoginWorkflow() {
        System.out.println("=== Complete Login Workflow Test ===");
        
        // Navigate to login page
        wrapper.navigateTo("https://the-internet.herokuapp.com/login");
        System.out.println("Navigated to: " + wrapper.getCurrentUrl());
        
        // Verify login form elements are present
        Assert.assertTrue(wrapper.isElementVisible(By.id("username")), "Username field should be visible");
        Assert.assertTrue(wrapper.isElementVisible(By.id("password")), "Password field should be visible");
        Assert.assertTrue(wrapper.isElementVisible(By.cssSelector("button[type='submit']")), "Login button should be visible");
        
        // Perform login
        wrapper.type(By.id("username"), "tomsmith");
        wrapper.type(By.id("password"), "SuperSecretPassword!");
        wrapper.click(By.cssSelector("button[type='submit']"));
        
        // Verify successful login
        wrapper.waitForElementVisible(By.cssSelector(".flash.success"));
        String successMessage = wrapper.getText(By.cssSelector(".flash.success"));
        Assert.assertTrue(successMessage.contains("You logged into a secure area!"));
        
        // Verify we're on the secure page
        Assert.assertTrue(wrapper.getCurrentUrl().contains("/secure"));
        Assert.assertTrue(wrapper.isElementVisible(By.xpath("//a[@href='/logout']")));
        
        System.out.println("Login workflow completed successfully!");
    }
    
    @Test
    public void testFormInteractionWorkflow() {
        System.out.println("=== Form Interaction Workflow Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/dropdown");
        
        // Test dropdown interactions
        wrapper.selectByText(By.id("dropdown"), "Option 1");
        Assert.assertEquals(wrapper.getSelectedText(By.id("dropdown")), "Option 1");
        System.out.println("Selected Option 1 from dropdown");
        
        wrapper.selectByValue(By.id("dropdown"), "2");
        Assert.assertEquals(wrapper.getSelectedText(By.id("dropdown")), "Option 2");
        System.out.println("Selected Option 2 from dropdown");
        
        // Navigate to checkboxes page
        wrapper.navigateTo("https://the-internet.herokuapp.com/checkboxes");
        
        By checkbox1 = By.xpath("//input[@type='checkbox'][1]");
        By checkbox2 = By.xpath("//input[@type='checkbox'][2]");
        
        // Test checkbox interactions
        boolean initialState1 = wrapper.isChecked(checkbox1);
        boolean initialState2 = wrapper.isChecked(checkbox2);
        
        wrapper.check(checkbox1);
        wrapper.uncheck(checkbox2);
        
        Assert.assertTrue(wrapper.isChecked(checkbox1));
        Assert.assertFalse(wrapper.isChecked(checkbox2));
        
        System.out.println("Checkbox interactions completed successfully!");
    }
    
    @Test
    public void testDynamicContentHandling() {
        System.out.println("=== Dynamic Content Handling Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/dynamic_loading/2");
        
        // Verify initial state
        Assert.assertFalse(wrapper.isElementVisible(By.xpath("//div[@id='finish']/h4")));
        
        // Start dynamic loading
        wrapper.click(By.xpath("//button[text()='Start']"));
        System.out.println("Started dynamic loading...");
        
        // Wait for loading to complete
        wrapper.waitForElementVisible(By.xpath("//div[@id='finish']/h4"));
        
        // Verify content appeared
        String finishText = wrapper.getText(By.xpath("//div[@id='finish']/h4"));
        Assert.assertEquals(finishText, "Hello World!");
        
        System.out.println("Dynamic content loaded successfully: " + finishText);
    }
    
    @Test
    public void testAlertHandlingWorkflow() {
        System.out.println("=== Alert Handling Workflow Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/javascript_alerts");
        
        // Test 1: Simple Alert
        wrapper.click(By.xpath("//button[text()='Click for JS Alert']"));
        String alertText = wrapper.getAlertText();
        System.out.println("Alert text: " + alertText);
        wrapper.acceptAlert();
        
        String result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You successfully clicked an alert");
        System.out.println("Simple alert handled successfully");
        
        // Test 2: Confirm Alert (Accept)
        wrapper.click(By.xpath("//button[text()='Click for JS Confirm']"));
        wrapper.acceptAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You clicked: Ok");
        System.out.println("Confirm alert accepted successfully");
        
        // Test 3: Confirm Alert (Dismiss)
        wrapper.click(By.xpath("//button[text()='Click for JS Confirm']"));
        wrapper.dismissAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You clicked: Cancel");
        System.out.println("Confirm alert dismissed successfully");
        
        // Test 4: Prompt Alert
        wrapper.click(By.xpath("//button[text()='Click for JS Prompt']"));
        wrapper.typeInAlert("Automated Test Input");
        wrapper.acceptAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You entered: Automated Test Input");
        System.out.println("Prompt alert handled successfully");
    }
    
    @Test
    public void testNavigationAndUtilityMethods() {
        System.out.println("=== Navigation and Utility Methods Test ===");
        
        // Navigate to initial page
        wrapper.navigateTo("https://the-internet.herokuapp.com/");
        String initialUrl = wrapper.getCurrentUrl();
        String initialTitle = wrapper.getTitle();
        
        System.out.println("Initial URL: " + initialUrl);
        System.out.println("Initial Title: " + initialTitle);
        
        // Navigate to another page
        wrapper.click(By.linkText("A/B Testing"));
        wrapper.waitForElementVisible(By.xpath("//h3[text()='A/B Test Variation 1']"));
        
        String newUrl = wrapper.getCurrentUrl();
        Assert.assertTrue(newUrl.contains("/abtest"));
        System.out.println("Navigated to: " + newUrl);
        
        // Test back navigation
        wrapper.back();
        Assert.assertEquals(wrapper.getCurrentUrl(), initialUrl);
        System.out.println("Back navigation successful");
        
        // Test forward navigation
        wrapper.forward();
        Assert.assertTrue(wrapper.getCurrentUrl().contains("/abtest"));
        System.out.println("Forward navigation successful");
        
        // Test refresh
        wrapper.refresh();
        Assert.assertTrue(wrapper.getCurrentUrl().contains("/abtest"));
        System.out.println("Page refresh successful");
    }
    
    @Test
    public void testScrollAndJavaScriptExecution() {
        System.out.println("=== Scroll and JavaScript Execution Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/large");
        
        // Get initial scroll position
        Long initialScroll = (Long) wrapper.executeScript("return window.pageYOffset;");
        System.out.println("Initial scroll position: " + initialScroll);
        
        // Scroll to bottom
        wrapper.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        
        // Verify scroll position changed
        Long finalScroll = (Long) wrapper.executeScript("return window.pageYOffset;");
        System.out.println("Final scroll position: " + finalScroll);
        Assert.assertTrue(finalScroll > initialScroll);
        
        // Test scrolling to specific element
        wrapper.navigateTo("https://the-internet.herokuapp.com/");
        wrapper.scrollToElement(By.linkText("Sortable Data Tables"));
        
        // Verify element is in view
        Boolean isInView = (Boolean) wrapper.executeScript(
            "var element = arguments[0];" +
            "var rect = element.getBoundingClientRect();" +
            "return rect.top >= 0 && rect.bottom <= window.innerHeight;",
            wrapper.findElement(By.linkText("Sortable Data Tables"))
        );
        
        System.out.println("Element in view after scroll: " + isInView);
    }
}
