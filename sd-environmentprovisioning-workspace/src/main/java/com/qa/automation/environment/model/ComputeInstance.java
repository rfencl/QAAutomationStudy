package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComputeInstance {
    private String id;
    private String instanceType;
    private String publicIp;
    private String privateIp;
    private String status;
}
