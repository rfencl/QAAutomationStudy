package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.Map;

/**
 * Result of environment provisioning operation
 */
@Data
@Builder
public class ProvisioningResult {
    private String environmentId;
    private boolean success;
    private EnvironmentInstance environment;
    private String errorMessage;
    private Exception exception;
    private Instant timestamp;
    private Map<String, String> endpoints;

    public static ProvisioningResult success(String environmentId, EnvironmentInstance environment) {
        return ProvisioningResult.builder()
            .environmentId(environmentId)
            .success(true)
            .environment(environment)
            .timestamp(Instant.now())
            .build();
    }

    public static ProvisioningResult failure(String environmentId, Exception exception) {
        return ProvisioningResult.builder()
            .environmentId(environmentId)
            .success(false)
            .exception(exception)
            .errorMessage(exception.getMessage())
            .timestamp(Instant.now())
            .build();
    }
}
