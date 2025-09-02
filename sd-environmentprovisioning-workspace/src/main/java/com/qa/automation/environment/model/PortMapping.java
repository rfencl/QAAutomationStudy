package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortMapping {
    private int containerPort;
    private int servicePort;
    private String protocol;
}