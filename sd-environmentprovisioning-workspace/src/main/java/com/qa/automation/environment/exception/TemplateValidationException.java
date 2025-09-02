package com.qa.automation.environment.exception;

/**
 * Exception thrown when template validation fails.
 */
public class TemplateValidationException extends ProvisioningException {
    
    public TemplateValidationException(String message) {
        super(message);
    }
}
