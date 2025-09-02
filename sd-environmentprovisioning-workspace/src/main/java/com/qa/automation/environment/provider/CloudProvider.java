package com.qa.automation.environment.provider;

/**
 * Interface for cloud provider implementations.
 * 
 * Design Decision: Strategy pattern allows supporting multiple cloud
 * providers (AWS, Azure, GCP) with consistent interface.
 */
public interface CloudProvider {
    
    /**
     * Gets the provider name (e.g., "aws", "azure", "gcp").
     */
    String getProviderName();
    
    /**
     * Creates a resource group for environment isolation.
     */
    String createResourceGroup(String name, String region);
    
    /**
     * Deletes a resource group and all contained resources.
     */
    void deleteResourceGroup(String resourceGroupName);
    
    /**
     * Checks if the provider is available and configured.
     */
    boolean isAvailable();
}
