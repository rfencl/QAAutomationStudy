package com.qa.tests;

import com.qa.config.Environment;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EnvironmentTest {
    
    @Test
    public void testEnvironmentFromString() {
        Assert.assertEquals(Environment.fromString("dev"), Environment.DEV);
        Assert.assertEquals(Environment.fromString("DEV"), Environment.DEV);
        Assert.assertEquals(Environment.fromString("qa"), Environment.QA);
        Assert.assertEquals(Environment.fromString("staging"), Environment.STAGING);
        Assert.assertEquals(Environment.fromString("prod"), Environment.PROD);
    }
    
    @Test
    public void testEnvironmentDefaultValue() {
        Environment env = Environment.fromString("invalid");
        Assert.assertEquals(env, Environment.DEV, "Should default to DEV for invalid environment");
        
        Environment nullEnv = Environment.fromString(null);
        Assert.assertEquals(nullEnv, Environment.DEV, "Should default to DEV for null environment");
    }
    
    @Test
    public void testEnvironmentNames() {
        Assert.assertEquals(Environment.DEV.getName(), "dev");
        Assert.assertEquals(Environment.QA.getName(), "qa");
        Assert.assertEquals(Environment.STAGING.getName(), "staging");
        Assert.assertEquals(Environment.PROD.getName(), "prod");
    }
    
    @Test
    public void testAllEnvironments() {
        Environment[] environments = Environment.values();
        Assert.assertEquals(environments.length, 4, "Should have 4 environments");
        
        boolean hasAll = false;
        for (Environment env : environments) {
            if (env == Environment.DEV || env == Environment.QA || 
                env == Environment.STAGING || env == Environment.PROD) {
                hasAll = true;
            }
        }
        Assert.assertTrue(hasAll, "Should contain all expected environments");
    }
}
