package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Deployed application instance
 */
@Data
@Builder
public class ApplicationInstance {
    private String name;
    private String deploymentId;
    private ApplicationType type;
    private ApplicationStatus status;
    private List<String> endpoints;
    private Map<String, String> configuration;
    private int replicas;
    private HealthStatus healthStatus;
}


