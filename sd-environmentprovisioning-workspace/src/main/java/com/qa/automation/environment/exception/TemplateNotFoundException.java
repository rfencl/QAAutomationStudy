package com.qa.automation.environment.exception;

/**
 * Exception thrown when a requested template is not found.
 */
public class TemplateNotFoundException extends ProvisioningException {
    
    public TemplateNotFoundException(String message) {
        super(message);
    }
}
