package com.qa.tests;

import com.qa.pages.DropdownPage;
import com.qa.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DropdownTest extends BaseTest {
    
    @Test
    public void testDropdownSelectionByValue() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickDropdown();
        
        DropdownPage dropdownPage = pageFactory.getDropdownPage();
        dropdownPage.selectByValue("1");
        
        Assert.assertEquals(dropdownPage.getSelectedOption(), "Option 1");
    }
    
    @Test
    public void testDropdownSelectionByText() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickDropdown();
        
        DropdownPage dropdownPage = pageFactory.getDropdownPage();
        dropdownPage.selectByText("Option 2");
        
        Assert.assertEquals(dropdownPage.getSelectedOption(), "Option 2");
    }
    
    @Test
    public void testPageHeading() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickDropdown();
        
        DropdownPage dropdownPage = pageFactory.getDropdownPage();
        Assert.assertEquals(dropdownPage.getPageHeading(), "Dropdown List");
    }
    
    @Test
    public void testMultipleSelections() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickDropdown();
        
        DropdownPage dropdownPage = pageFactory.getDropdownPage();
        
        dropdownPage.selectByValue("1");
        Assert.assertEquals(dropdownPage.getSelectedOption(), "Option 1");
        
        dropdownPage.selectByValue("2");
        Assert.assertEquals(dropdownPage.getSelectedOption(), "Option 2");
    }
}
