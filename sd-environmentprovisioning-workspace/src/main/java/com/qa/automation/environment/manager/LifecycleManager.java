package com.qa.automation.environment.manager;

import com.qa.automation.environment.model.*;
import com.qa.automation.environment.exception.EnvironmentCleanupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages environment lifecycle including automatic cleanup and TTL management.
 * 
 * Design Decision: Uses scheduled executor for automatic cleanup to prevent
 * resource waste and cost overruns. Maintains environment registry for
 * tracking and management.
 */
@Slf4j
@RequiredArgsConstructor
public class LifecycleManager {
    
    private final Map<String, EnvironmentInstance> environmentRegistry = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final InfrastructureManager infrastructureManager;
    private final DatabaseManager databaseManager;
    private final ApplicationDeploymentManager applicationManager;
    private final ConfigurationManager configurationManager;

    /**
     * Registers an environment for lifecycle management.
     */
    public void registerEnvironment(EnvironmentInstance environment) {
        log.info("Registering environment for lifecycle management: {}", environment.getId());
        
        environmentRegistry.put(environment.getId(), environment);
        
        // Schedule automatic cleanup if TTL is specified
        if (environment.getTtl() != null) {
            scheduleCleanup(environment.getId(), environment.getTtl().toMillis());
        }
        
        log.info("Environment registered successfully: {}", environment.getId());
    }

    /**
     * Schedules automatic cleanup for an environment.
     */
    private void scheduleCleanup(String environmentId, long ttlMillis) {
        log.info("Scheduling cleanup for environment: {} in {} ms", environmentId, ttlMillis);
        
        scheduler.schedule(() -> {
            try {
                log.info("Executing scheduled cleanup for environment: {}", environmentId);
                cleanupEnvironment(environmentId);
            } catch (Exception e) {
                log.error("Scheduled cleanup failed for environment: {}", environmentId, e);
            }
        }, ttlMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Manually triggers environment cleanup.
     */
    public void cleanupEnvironment(String environmentId) {
        log.info("Starting cleanup for environment: {}", environmentId);
        
        EnvironmentInstance environment = environmentRegistry.get(environmentId);
        if (environment == null) {
            log.warn("Environment not found for cleanup: {}", environmentId);
            return;
        }
        
        try {
            // Update status to indicate cleanup in progress
            environment.setStatus(EnvironmentStatus.TERMINATING);
            
            // Stop applications
            stopApplications(environment.getApplications());
            
            // Cleanup databases
            cleanupDatabases(environment.getDatabases());
            
            // Release infrastructure resources
            releaseInfrastructure(environment.getInfrastructure());
            
            // Remove configurations
            configurationManager.removeConfigurations(environmentId);
            
            // Update final status
            environment.setStatus(EnvironmentStatus.TERMINATED);
            environment.setEndTime(Instant.now());
            
            log.info("Environment cleanup completed successfully: {}", environmentId);
            
        } catch (Exception e) {
            log.error("Environment cleanup failed: {}", environmentId, e);
            environment.setStatus(EnvironmentStatus.CLEANUP_FAILED);
            throw new EnvironmentCleanupException("Failed to cleanup environment: " + environmentId, e);
        }
    }

    /**
     * Stops all applications in the environment.
     */
    private void stopApplications(java.util.List<ApplicationInstance> applications) {
        if (applications == null || applications.isEmpty()) {
            return;
        }
        
        log.info("Stopping {} applications", applications.size());
        
        applications.forEach(app -> {
            log.info("Stopping application: {}", app.getName());
            app.setStatus(ApplicationStatus.STOPPED);
        });
    }

    /**
     * Cleans up database instances.
     */
    private void cleanupDatabases(java.util.List<DatabaseInstance> databases) {
        if (databases == null || databases.isEmpty()) {
            return;
        }
        
        log.info("Cleaning up {} databases", databases.size());
        
        databases.forEach(db -> {
            log.info("Cleaning up database: {}", db.getName());
            // In real implementation, would terminate database instances
            db.setStatus("Terminated");
        });
    }

    /**
     * Releases infrastructure resources.
     */
    private void releaseInfrastructure(InfrastructureResources infrastructure) {
        if (infrastructure == null) {
            return;
        }
        
        log.info("Releasing infrastructure resources for resource group: {}", 
            infrastructure.getResourceGroup());
        
        // Release container cluster
        if (infrastructure.getContainerCluster() != null) {
            log.info("Releasing container cluster: {}", 
                infrastructure.getContainerCluster().getName());
            infrastructure.getContainerCluster().setStatus("Terminated");
        }
        
        // Release compute instances
        if (infrastructure.getComputeInstances() != null) {
            infrastructure.getComputeInstances().forEach(instance -> {
                log.info("Releasing compute instance: {}", instance.getId());
                instance.setStatus("Terminated");
            });
        }
    }

    /**
     * Gets the current status of an environment.
     */
    public EnvironmentStatus getEnvironmentStatus(String environmentId) {
        EnvironmentInstance environment = environmentRegistry.get(environmentId);
        return environment != null ? environment.getStatus() : null;
    }

    /**
     * Gets environment details.
     */
    public EnvironmentInstance getEnvironment(String environmentId) {
        return environmentRegistry.get(environmentId);
    }

    /**
     * Periodic cleanup of expired environments.
     * Runs every hour to check for environments that should be cleaned up.
     */
    public void startPeriodicCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredEnvironments();
            } catch (Exception e) {
                log.error("Periodic cleanup failed", e);
            }
        }, 1, 1, TimeUnit.HOURS);
        
        log.info("Periodic cleanup scheduler started");
    }

    /**
     * Cleans up environments that have exceeded their TTL.
     */
    private void cleanupExpiredEnvironments() {
        Instant now = Instant.now();
        
        environmentRegistry.values().stream()
            .filter(env -> env.getTtl() != null)
            .filter(env -> env.getStartTime().plus(env.getTtl()).isBefore(now))
            .filter(env -> env.getStatus() == EnvironmentStatus.READY)
            .forEach(env -> {
                log.info("Environment expired, scheduling cleanup: {}", env.getId());
                try {
                    cleanupEnvironment(env.getId());
                } catch (Exception e) {
                    log.error("Failed to cleanup expired environment: {}", env.getId(), e);
                }
            });
    }

    /**
     * Shuts down the lifecycle manager and cleanup scheduler.
     */
    public void shutdown() {
        log.info("Shutting down lifecycle manager");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
