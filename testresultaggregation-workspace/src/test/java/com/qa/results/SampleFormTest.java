package com.qa.results;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleFormTest {
    
    @Test
    public void testFormSubmission() {
        simulateTestExecution(900);
        Assert.assertTrue(true, "Form submission successful");
    }
    
    @Test
    public void testFormValidation() {
        simulateTestExecution(1100);
        Assert.assertTrue(true, "Form validation working");
    }
    
    @Test
    public void testDropdownSelection() {
        simulateTestExecution(600);
        Assert.assertTrue(true, "Dropdown selection working");
    }
    
    @Test
    public void testFileUpload() {
        simulateTestExecution(3000);
        // Simulate a failure
        Assert.fail("File upload timeout");
    }
    
    @Test(enabled = false)
    public void testDisabledFeature() {
        simulateTestExecution(100);
        Assert.assertTrue(true, "This test is disabled");
    }
    
    private void simulateTestExecution(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
