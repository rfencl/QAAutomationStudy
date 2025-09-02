package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SecurityGroupSpec {
    private String name;
    private String description;
    private List<SecurityRule> rules;
}
