package com.qa.factory;

import com.qa.pages.*;
import org.openqa.selenium.WebDriver;
import java.util.concurrent.ConcurrentHashMap;

public class PageObjectFactory {
    private final WebDriver driver;
    private final ConcurrentHashMap<Class<?>, Object> pageCache;
    
    public PageObjectFactory(WebDriver driver) {
        this.driver = driver;
        this.pageCache = new ConcurrentHashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return (T) pageCache.computeIfAbsent(pageClass, this::createPage);
    }
    
    private Object createPage(Class<?> pageClass) {
        try {
            return pageClass.getConstructor(WebDriver.class).newInstance(driver);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create page object: " + pageClass.getSimpleName(), e);
        }
    }
    
    public HomePage getHomePage() {
        return getPage(HomePage.class);
    }
    
    public LoginPage getLoginPage() {
        return getPage(LoginPage.class);
    }
    
    public CheckboxesPage getCheckboxesPage() {
        return getPage(CheckboxesPage.class);
    }
    
    public DropdownPage getDropdownPage() {
        return getPage(DropdownPage.class);
    }
    
    public void clearCache() {
        pageCache.clear();
    }
    
    public int getCacheSize() {
        return pageCache.size();
    }
}
