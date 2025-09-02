package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Internal provisioning plan created from template and request
 */
@Data
@Builder
public class ProvisioningPlan {
    private String templateId;
    private InfrastructureSpec infrastructureSpec;
    private NetworkSpec networkSpec;
    private List<DatabaseSpec> databaseSpecs;
    private List<ApplicationSpec> applicationSpecs;
    private Map<String, Object> configurations;
    private Map<String, Object> parameters;
}
