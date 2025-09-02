package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceRequirements {
    private String cpu;
    private String memory;
    private String storage;
}
