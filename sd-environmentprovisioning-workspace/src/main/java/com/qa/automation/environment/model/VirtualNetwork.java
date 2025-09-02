package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VirtualNetwork {
    private String id;
    private String name;
    private String cidr;
    private String status;
}
