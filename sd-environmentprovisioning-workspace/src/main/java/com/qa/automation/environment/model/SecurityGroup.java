package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class SecurityGroup {
    private String id;
    private String name;
    private List<SecurityRule> rules;
}
