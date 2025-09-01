package com.qa.tests;

import com.qa.pages.HomePage;
import com.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PageObjectFactoryTest extends BaseTest {
    
    @Test
    public void testPageObjectCreation() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage1 = pageFactory.getHomePage();
        HomePage homePage2 = pageFactory.getHomePage();
        
        // Should return same instance (cached)
        Assert.assertSame(homePage1, homePage2);
        Assert.assertEquals(pageFactory.getCacheSize(), 1);
    }
    
    @Test
    public void testMultiplePageObjects() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getHomePage();
        LoginPage loginPage = pageFactory.getLoginPage();
        
        Assert.assertNotNull(homePage);
        Assert.assertNotNull(loginPage);
        Assert.assertNotSame(homePage, loginPage);
        Assert.assertEquals(pageFactory.getCacheSize(), 2);
    }
    
    @Test
    public void testCacheClear() {
        driver.get("https://the-internet.herokuapp.com/");
        
        pageFactory.getHomePage();
        pageFactory.getLoginPage();
        Assert.assertEquals(pageFactory.getCacheSize(), 2);
        
        pageFactory.clearCache();
        Assert.assertEquals(pageFactory.getCacheSize(), 0);
    }
    
    @Test
    public void testGenericPageCreation() {
        driver.get("https://the-internet.herokuapp.com/");
        
        HomePage homePage = pageFactory.getPage(HomePage.class);
        LoginPage loginPage = pageFactory.getPage(LoginPage.class);
        
        Assert.assertNotNull(homePage);
        Assert.assertNotNull(loginPage);
        Assert.assertTrue(homePage.getTitle().contains("The Internet"));
    }
}
