package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class NetworkConfig {
    private String subnetId;
    private List<String> securityGroupIds;
    private boolean publicAccess;
}