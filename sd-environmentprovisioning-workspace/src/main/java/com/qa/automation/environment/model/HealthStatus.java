package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthStatus {
    private boolean healthy;
    private String lastCheck;
    private String message;
}
