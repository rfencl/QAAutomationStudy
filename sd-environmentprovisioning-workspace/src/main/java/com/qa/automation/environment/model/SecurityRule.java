package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SecurityRule {
    private int port;
    private String protocol;
    private String source;
    private String direction;
}
