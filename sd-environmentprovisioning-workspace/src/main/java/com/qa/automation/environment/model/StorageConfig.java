package com.qa.automation.environment.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageConfig {
    private int sizeGb;
    private String storageType;
    private boolean encrypted;
}