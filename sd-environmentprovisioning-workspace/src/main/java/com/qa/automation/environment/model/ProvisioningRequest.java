package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.time.Duration;
import java.util.Map;

/**
 * Request for environment provisioning
 */
@Data
@Builder
public class ProvisioningRequest {
    private String templateId;
    private String requestedBy;
    private Duration ttl;
    private Map<String, Object> parameters;
    private Map<String, String> tags;
}
