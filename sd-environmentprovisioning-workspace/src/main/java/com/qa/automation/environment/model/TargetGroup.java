package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TargetGroup {
    private String name;
    private int port;
    private String protocol;
    private String healthCheckPath;
}
