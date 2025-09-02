package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VolumeMount {
    private String name;
    private String mountPath;
    private String hostPath;
}
