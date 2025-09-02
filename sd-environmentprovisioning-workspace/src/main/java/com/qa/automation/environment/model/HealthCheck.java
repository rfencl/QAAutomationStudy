package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthCheck {
    private String path;
    private int port;
    private int initialDelaySeconds;
    private int periodSeconds;
}
