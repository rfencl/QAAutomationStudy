package com.qa.db;

public class DatabaseConfig {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
    private int minPoolSize = 5;
    private int maxPoolSize = 20;
    private int connectionTimeout = 30000; // 30 seconds
    private int idleTimeout = 600000; // 10 minutes
    private int maxLifetime = 1800000; // 30 minutes
    
    public DatabaseConfig(String url, String username, String password, String driverClassName) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = driverClassName;
    }
    
    // Getters and Setters
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
    
    public int getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(int minPoolSize) { this.minPoolSize = minPoolSize; }
    
    public int getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(int maxPoolSize) { this.maxPoolSize = maxPoolSize; }
    
    public int getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }
    
    public int getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(int idleTimeout) { this.idleTimeout = idleTimeout; }
    
    public int getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(int maxLifetime) { this.maxLifetime = maxLifetime; }
}
