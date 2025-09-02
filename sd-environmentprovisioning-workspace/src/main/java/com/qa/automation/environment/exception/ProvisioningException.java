package com.qa.automation.environment.exception;

/**
 * Base exception for environment provisioning errors.
 */
public class ProvisioningException extends RuntimeException {
    
    public ProvisioningException(String message) {
        super(message);
    }
    
    public ProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
