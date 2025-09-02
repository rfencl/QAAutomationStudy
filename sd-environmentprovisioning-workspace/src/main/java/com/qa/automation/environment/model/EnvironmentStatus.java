package com.qa.automation.environment.model;

/**
 * Environment lifecycle status enumeration
 */
public enum EnvironmentStatus {
    REQUESTED,
    PROVISIONING,
    READY,
    FAILED,
    TERMINATING,
    TERMINATED,
    CLEANUP_FAILED
}
