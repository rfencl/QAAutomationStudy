package com.qa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationManager {
    private static ConfigurationManager instance;
    private final ConcurrentHashMap<String, Object> configCache = new ConcurrentHashMap<>();
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private Environment currentEnvironment;
    
    private ConfigurationManager() {
        this.currentEnvironment = Environment.fromString(System.getProperty("env", "dev"));
        loadConfigurations();
    }
    
    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (ConfigurationManager.class) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }
    
    private void loadConfigurations() {
        loadPropertiesFile("application.properties");
        loadPropertiesFile("application-" + currentEnvironment.getName() + ".properties");
        loadYamlFile("config.yaml");
    }
    
    private void loadPropertiesFile(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/" + filename)) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                props.forEach((key, value) -> configCache.put(key.toString(), value));
            }
        } catch (Exception e) {
            // File not found or error loading - continue silently
        }
    }
    
    private void loadYamlFile(String filename) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config/" + filename)) {
            if (is != null) {
                ConfigData config = yamlMapper.readValue(is, ConfigData.class);
                configCache.put("browser.type", config.getBrowser().getType());
                configCache.put("browser.headless", config.getBrowser().isHeadless());
                configCache.put("browser.timeout", config.getBrowser().getTimeout());
                configCache.put("app.url", config.getApp().getUrl());
                configCache.put("app.name", config.getApp().getName());
                configCache.put("database.url", config.getDatabase().getUrl());
                configCache.put("database.username", config.getDatabase().getUsername());
                configCache.put("database.password", config.getDatabase().getPassword());
            }
        } catch (Exception e) {
            // File not found or error loading - continue silently
        }
    }
    
    public String getString(String key) {
        return getString(key, null);
    }
    
    public String getString(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) return systemValue;
        
        String envValue = System.getenv(key.replace(".", "_").toUpperCase());
        if (envValue != null) return envValue;
        
        Object value = configCache.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    public int getInt(String key, int defaultValue) {
        String value = getString(key);
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }
    
    public Environment getCurrentEnvironment() {
        return currentEnvironment;
    }
    
    public void setProperty(String key, Object value) {
        configCache.put(key, value);
    }
    
    public void clearCache() {
        configCache.clear();
        loadConfigurations();
    }
}
