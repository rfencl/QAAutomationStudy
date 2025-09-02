package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Template definition for environment provisioning
 */
@Data
@Builder
public class EnvironmentTemplate {
    private String id;
    private String name;
    private String description;
    private String version;
    private InfrastructureSpec infrastructureSpec;
    private List<ApplicationSpec> applicationSpecs;
    private List<DatabaseSpec> databaseSpecs;
    private NetworkSpec networkSpec;
    private Map<String, Object> configurations;
    private List<String> tags;
}
