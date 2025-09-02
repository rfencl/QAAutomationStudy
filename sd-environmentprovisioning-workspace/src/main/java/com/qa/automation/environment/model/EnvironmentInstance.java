package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Represents a provisioned test environment instance
 */
@Data
@Builder
public class EnvironmentInstance {
    private String id;
    private String templateId;
    private EnvironmentStatus status;
    private Instant startTime;
    private Instant endTime;
    private Duration ttl;
    private InfrastructureResources infrastructure;
    private NetworkConfiguration network;
    private List<DatabaseInstance> databases;
    private List<ApplicationInstance> applications;
    private Map<String, String> endpoints;
    private Map<String, Object> metadata;
}
