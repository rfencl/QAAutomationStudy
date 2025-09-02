package com.qa.automation.environment.service;

import com.qa.automation.environment.model.EnvironmentTemplate;
import com.qa.automation.environment.exception.TemplateValidationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Validates environment templates for correctness and completeness.
 * 
 * Design Decision: Separate validator class follows single responsibility
 * principle and makes validation logic reusable and testable.
 */
@Slf4j
public class TemplateValidator {

    /**
     * Validates an environment template.
     * 
     * @param template The template to validate
     * @throws TemplateValidationException if validation fails
     */
    public void validate(EnvironmentTemplate template) {
        log.debug("Validating template: {}", template.getId());
        
        validateBasicFields(template);
        validateInfrastructureSpec(template);
        validateApplicationSpecs(template);
        validateDatabaseSpecs(template);
        
        log.debug("Template validation successful: {}", template.getId());
    }

    private void validateBasicFields(EnvironmentTemplate template) {
        if (template.getId() == null || template.getId().trim().isEmpty()) {
            throw new TemplateValidationException("Template ID is required");
        }
        
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            throw new TemplateValidationException("Template name is required");
        }
        
        if (template.getVersion() == null || template.getVersion().trim().isEmpty()) {
            throw new TemplateValidationException("Template version is required");
        }
    }

    private void validateInfrastructureSpec(EnvironmentTemplate template) {
        if (template.getInfrastructureSpec() == null) {
            throw new TemplateValidationException("Infrastructure specification is required");
        }
        
        var infraSpec = template.getInfrastructureSpec();
        if (infraSpec.getCloudProvider() == null || infraSpec.getCloudProvider().trim().isEmpty()) {
            throw new TemplateValidationException("Cloud provider is required");
        }
    }

    private void validateApplicationSpecs(EnvironmentTemplate template) {
        if (template.getApplicationSpecs() == null || template.getApplicationSpecs().isEmpty()) {
            throw new TemplateValidationException("At least one application specification is required");
        }
        
        template.getApplicationSpecs().forEach(appSpec -> {
            if (appSpec.getName() == null || appSpec.getName().trim().isEmpty()) {
                throw new TemplateValidationException("Application name is required");
            }
            
            if (appSpec.getDeploymentType() == null) {
                throw new TemplateValidationException("Application deployment type is required");
            }
        });
    }

    private void validateDatabaseSpecs(EnvironmentTemplate template) {
        if (template.getDatabaseSpecs() != null) {
            template.getDatabaseSpecs().forEach(dbSpec -> {
                if (dbSpec.getName() == null || dbSpec.getName().trim().isEmpty()) {
                    throw new TemplateValidationException("Database name is required");
                }
                
                if (dbSpec.getDatabaseType() == null || dbSpec.getDatabaseType().trim().isEmpty()) {
                    throw new TemplateValidationException("Database type is required");
                }
            });
        }
    }
}
