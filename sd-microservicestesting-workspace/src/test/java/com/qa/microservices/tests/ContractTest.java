package com.qa.microservices.tests;

import com.qa.microservices.testing.ContractTestBase;
import com.qa.microservices.virtualization.ServiceVirtualization;
import org.testng.annotations.*;
import org.testng.Assert;

/**
 * Contract testing implementation for microservices.
 * Design Decision: Extends ContractTestBase to implement specific contract validation logic.
 * This ensures API contracts are maintained between service versions.
 */
public class ContractTest extends ContractTestBase {
    
    private ServiceVirtualization serviceVirtualization;
    
    @BeforeClass
    public void setupClass() {
        serviceVirtualization = new ServiceVirtualization();
        serviceVirtualization.startServices();
    }
    
    @AfterClass
    public void teardownClass() {
        if (serviceVirtualization != null) {
            serviceVirtualization.stopServices();
        }
    }
    
    @Test(description = "Validate User Service API contract")
    public void testUserServiceContract() {
        String serviceUrl = "http://localhost:8081";
        String contractDefinition = "{\n" +
            "  \"service\": \"UserService\",\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"endpoints\": [\n" +
            "    {\n" +
            "      \"path\": \"/users/{id}\",\n" +
            "      \"method\": \"GET\",\n" +
            "      \"response\": {\n" +
            "        \"status\": 200,\n" +
            "        \"schema\": {\n" +
            "          \"type\": \"object\",\n" +
            "          \"properties\": {\n" +
            "            \"id\": {\"type\": \"string\"},\n" +
            "            \"username\": {\"type\": \"string\"},\n" +
            "            \"email\": {\"type\": \"string\"},\n" +
            "            \"status\": {\"type\": \"string\"}\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        boolean isValid = validateContract(serviceUrl, contractDefinition);
        Assert.assertTrue(isValid, "User Service contract should be valid");
    }
    
    @Test(description = "Test contract comparison for breaking changes")
    public void testContractComparison() {
        String oldContract = "{\n" +
            "  \"service\": \"UserService\",\n" +
            "  \"version\": \"1.0\",\n" +
            "  \"endpoints\": []\n" +
            "}";
        
        String newContract = "{\n" +
            "  \"service\": \"UserService\",\n" +
            "  \"version\": \"1.1\",\n" +
            "  \"endpoints\": [\n" +
            "    {\n" +
            "      \"path\": \"/users/{id}\",\n" +
            "      \"method\": \"GET\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
        
        boolean isCompatible = compareContracts(oldContract, newContract);
        Assert.assertTrue(isCompatible, "New contract should be compatible with old contract");
    }
    
    @Test(description = "Test contract generation")
    public void testContractGeneration() {
        String contract = generateContract("UserService", "1.0");
        
        Assert.assertNotNull(contract, "Generated contract should not be null");
        Assert.assertTrue(contract.contains("UserService"), "Contract should contain service name");
        Assert.assertTrue(contract.contains("1.0"), "Contract should contain version");
    }
    
    @Override
    public boolean validateContract(String serviceUrl, String contractDefinition) {
        logger.info("Validating contract for service: {}", serviceUrl);
        
        try {
            // Simulate contract validation by making actual API call
            io.restassured.response.Response response = io.restassured.RestAssured.given()
                .baseUri(serviceUrl)
                .get("/users/user-123");
            
            if (response.getStatusCode() != 200) {
                logValidationResult(serviceUrl, false, "Expected 200 status code");
                return false;
            }
            
            String responseBody = response.getBody().asString();
            boolean schemaValid = validateResponseSchema(responseBody, contractDefinition);
            
            logValidationResult(serviceUrl, schemaValid, "Schema validation completed");
            return schemaValid;
            
        } catch (Exception e) {
            logger.error("Contract validation failed", e);
            logValidationResult(serviceUrl, false, "Exception during validation: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public String generateContract(String serviceName, String version) {
        logger.info("Generating contract for service: {} version: {}", serviceName, version);
        
        // Simplified contract generation
        return String.format("{\n" +
            "  \"service\": \"%s\",\n" +
            "  \"version\": \"%s\",\n" +
            "  \"generated\": \"%s\",\n" +
            "  \"endpoints\": [\n" +
            "    {\n" +
            "      \"path\": \"/users/{id}\",\n" +
            "      \"method\": \"GET\",\n" +
            "      \"response\": {\n" +
            "        \"status\": 200,\n" +
            "        \"contentType\": \"application/json\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}", serviceName, version, java.time.LocalDateTime.now());
    }
}
