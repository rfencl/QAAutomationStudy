package com.qa.tests;

import com.qa.pages.CheckboxesPage;
import com.qa.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CheckboxesTest extends BaseTest {
    
    @Test
    public void testCheckboxInteraction() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickCheckboxes();
        
        CheckboxesPage checkboxesPage = pageFactory.getCheckboxesPage();
        Assert.assertEquals(checkboxesPage.getCheckboxCount(), 2);
        
        // First checkbox should be unchecked initially
        Assert.assertFalse(checkboxesPage.isCheckboxSelected(0));
        
        // Click first checkbox
        checkboxesPage.clickCheckbox(0);
        Assert.assertTrue(checkboxesPage.isCheckboxSelected(0));
        
        // Click again to uncheck
        checkboxesPage.clickCheckbox(0);
        Assert.assertFalse(checkboxesPage.isCheckboxSelected(0));
    }
    
    @Test
    public void testSecondCheckbox() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickCheckboxes();
        
        CheckboxesPage checkboxesPage = pageFactory.getCheckboxesPage();
        
        // Second checkbox should be checked initially
        Assert.assertTrue(checkboxesPage.isCheckboxSelected(1));
        
        // Click to uncheck
        checkboxesPage.clickCheckbox(1);
        Assert.assertFalse(checkboxesPage.isCheckboxSelected(1));
    }
    
    @Test
    public void testPageHeading() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickCheckboxes();
        
        CheckboxesPage checkboxesPage = pageFactory.getCheckboxesPage();
        Assert.assertEquals(checkboxesPage.getPageHeading(), "Checkboxes");
    }
}
