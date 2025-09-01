package com.qa.config;

public enum Environment {
    DEV("dev"),
    QA("qa"),
    STAGING("staging"),
    PROD("prod");
    
    private final String name;
    
    Environment(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public static Environment fromString(String env) {
        for (Environment e : values()) {
            if (e.name.equalsIgnoreCase(env)) {
                return e;
            }
        }
        return DEV; // default
    }
}
