package com.qa.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfigData {
    @JsonProperty("browser")
    private BrowserConfig browser;
    
    @JsonProperty("app")
    private AppConfig app;
    
    @JsonProperty("database")
    private DatabaseConfig database;
    
    public BrowserConfig getBrowser() { return browser; }
    public void setBrowser(BrowserConfig browser) { this.browser = browser; }
    
    public AppConfig getApp() { return app; }
    public void setApp(AppConfig app) { this.app = app; }
    
    public DatabaseConfig getDatabase() { return database; }
    public void setDatabase(DatabaseConfig database) { this.database = database; }
    
    public static class BrowserConfig {
        private String type;
        private boolean headless;
        private int timeout;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public boolean isHeadless() { return headless; }
        public void setHeadless(boolean headless) { this.headless = headless; }
        
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
    
    public static class AppConfig {
        private String url;
        private String name;
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
    
    public static class DatabaseConfig {
        private String url;
        private String username;
        private String password;
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
