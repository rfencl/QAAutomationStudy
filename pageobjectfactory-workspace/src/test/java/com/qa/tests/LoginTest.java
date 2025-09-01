package com.qa.tests;

import com.qa.pages.HomePage;
import com.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {
    
    @Test
    public void testValidLogin() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickFormAuthentication();
        
        LoginPage loginPage = pageFactory.getLoginPage();
        loginPage.enterUsername("tomsmith")
                .enterPassword("SuperSecretPassword!")
                .clickLogin();
        
        Assert.assertTrue(loginPage.isLoginSuccessful());
        Assert.assertTrue(loginPage.getFlashMessage().contains("You logged into a secure area!"));
    }
    
    @Test
    public void testInvalidLogin() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickFormAuthentication();
        
        LoginPage loginPage = pageFactory.getLoginPage();
        loginPage.enterUsername("invalid")
                .enterPassword("invalid")
                .clickLogin();
        
        Assert.assertFalse(loginPage.isLoginSuccessful());
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"));
    }
    
    @Test
    public void testEmptyCredentials() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        homePage.clickFormAuthentication();
        
        LoginPage loginPage = pageFactory.getLoginPage();
        loginPage.enterUsername("")
                .enterPassword("")
                .clickLogin();
        
        Assert.assertFalse(loginPage.isLoginSuccessful());
        Assert.assertTrue(loginPage.getFlashMessage().contains("Your username is invalid!"));
    }
}
