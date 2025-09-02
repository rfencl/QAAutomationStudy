package com.qa.automation.environment.api.dto;

import com.qa.automation.environment.model.EnvironmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.util.Map;

/**
 * DTO for API response after a provisioning request.
 */
@Data
@Builder
public class ProvisioningResponse {
    private String environmentId;
    private EnvironmentStatus status;
    private Map<String, String> endpoints;
    private Duration estimatedReadyTime;
}
