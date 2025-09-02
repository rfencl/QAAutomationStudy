package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoadBalancer {
    private String id;
    private String name;
    private String dnsName;
    private String type;
    private List<String> subnets;
}
