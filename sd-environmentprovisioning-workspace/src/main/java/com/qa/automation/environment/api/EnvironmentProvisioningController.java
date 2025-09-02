package com.qa.automation.environment.api;

import com.qa.automation.environment.api.dto.ProvisioningResponse;
import com.qa.automation.environment.core.EnvironmentProvisioningOrchestrator;
import com.qa.automation.environment.manager.LifecycleManager;
import com.qa.automation.environment.model.EnvironmentInstance;
import com.qa.automation.environment.model.EnvironmentStatus;
import com.qa.automation.environment.model.ProvisioningRequest;
import com.qa.automation.environment.model.ProvisioningResult;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

/**
 * API entry point for managing test environments.
 * In a real application, this would be a REST controller (e.g., using Spring MVC).
 */
@RequiredArgsConstructor
public class EnvironmentProvisioningController {

    private final EnvironmentProvisioningOrchestrator provisioningOrchestrator;
    private final LifecycleManager lifecycleManager;

    /**
     * Handles a request to provision a new test environment.
     * @param request The provisioning request.
     * @return A response containing the status and details of the provisioning attempt.
     */
    public ProvisioningResponse provisionEnvironment(ProvisioningRequest request) {
        ProvisioningResult result = provisioningOrchestrator.provisionEnvironment(request);

        if (!result.isSuccess()) {
            // In a real app, you'd handle this with proper error responses.
            return ProvisioningResponse.builder()
                    .environmentId(result.getEnvironmentId())
                    .status(result.getEnvironment() != null ? result.getEnvironment().getStatus() : EnvironmentStatus.FAILED)
                    .build();
        }

        return ProvisioningResponse.builder()
                .environmentId(result.getEnvironmentId())
                .status(result.getEnvironment().getStatus())
                .endpoints(result.getEnvironment().getEndpoints())
                .estimatedReadyTime(Duration.ofMinutes(5)) // Placeholder value
                .build();
    }

    /**
     * Retrieves the status of a specific environment.
     * @param environmentId The ID of the environment.
     * @return The current status of the environment.
     */
    public EnvironmentStatus getEnvironmentStatus(String environmentId) {
        EnvironmentInstance environment = lifecycleManager.getEnvironment(environmentId);
        if (environment == null) {
            return null; // In a real app, this would be a 404 Not Found response.
        }
        return environment.getStatus();
    }

    /**
     * Triggers the cleanup of a specific environment.
     * @param environmentId The ID of the environment to clean up.
     */
    public void cleanupEnvironment(String environmentId) {
        lifecycleManager.cleanupEnvironment(environmentId);
    }
}
