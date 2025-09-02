package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubnetSpec {
    private String name;
    private String cidr;
    private String type;
    private String availabilityZone;
}
