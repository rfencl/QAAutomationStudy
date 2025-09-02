package com.qa.automation.environment.exception;

/**
 * Exception thrown when environment cleanup fails.
 */
public class EnvironmentCleanupException extends ProvisioningException {
    
    public EnvironmentCleanupException(String message) {
        super(message);
    }
    
    public EnvironmentCleanupException(String message, Throwable cause) {
        super(message, cause);
    }
}
