# SD Microservices Testing Workspace

## Overview

This workspace demonstrates comprehensive testing strategies for microservices architecture, including service virtualization, contract testing, and end-to-end testing. It provides a complete framework for testing distributed systems with proper isolation, mocking, and validation.

## Architecture & Design Decisions

### 1. Service Virtualization with WireMock
**Design Decision**: Use WireMock for service virtualization to provide controlled, predictable responses for dependent services.

**Justification**:
- **Isolation**: Tests can run independently without requiring actual microservices
- **Predictability**: Consistent responses enable reliable test execution
- **Edge Case Testing**: Easy simulation of error conditions, timeouts, and edge cases
- **Performance**: Fast execution without network dependencies

### 2. Contract Testing Framework
**Design Decision**: Implement consumer-driven contract testing to ensure API compatibility.

**Justification**:
- **Early Detection**: Identifies breaking changes before deployment
- **Service Independence**: Teams can develop services independently while maintaining compatibility
- **Documentation**: Contracts serve as living documentation of API expectations
- **Regression Prevention**: Prevents accidental API changes that break consumers

### 3. End-to-End Test Orchestration
**Design Decision**: Create an orchestrator to coordinate complex business workflows across multiple services.

**Justification**:
- **Business Validation**: Tests complete user journeys and business processes
- **Integration Verification**: Validates that services work together correctly
- **Workflow Testing**: Ensures proper state transitions and data flow
- **System Confidence**: Provides confidence in the entire system behavior

### 4. Service Client Abstraction
**Design Decision**: Encapsulate service interactions in dedicated client classes.

**Justification**:
- **Reusability**: Clients can be reused across different test scenarios
- **Error Handling**: Centralized error handling and response parsing
- **Maintainability**: Changes to service APIs only require updates in one place
- **Testability**: Easy to mock and test service interactions

## Key Components

### Models
- **User**: Represents user entity with basic properties
- **Order**: Complex business entity with relationships and business logic
- **OrderItem**: Supporting entity for order composition

### Service Clients
- **UserServiceClient**: Handles all User Service interactions
- **OrderServiceClient**: Manages Order Service operations

### Testing Framework
- **ServiceVirtualization**: WireMock-based service mocking
- **ContractTestBase**: Foundation for contract testing
- **EndToEndTestOrchestrator**: Coordinates complex test workflows

## Test Scenarios Covered

### 1. Service Virtualization Tests
- ✅ Successful service responses
- ✅ Error handling (404, 500 errors)
- ✅ Performance testing (slow responses)
- ✅ Health check validation

### 2. Contract Tests
- ✅ API contract validation
- ✅ Schema compliance verification
- ✅ Breaking change detection
- ✅ Contract generation and comparison

### 3. End-to-End Tests
- ✅ Complete order workflow testing
- ✅ Service resilience validation
- ✅ Cross-service performance testing
- ✅ Concurrent operation testing
- ✅ Invalid data handling

## Architecture Diagrams

### Class Diagram
The class diagram (`docs/class-diagram.puml`) shows:
- **Model Layer**: Domain objects with proper encapsulation
- **Client Layer**: Service interaction abstractions
- **Testing Layer**: Framework components for different testing strategies

### Sequence Diagram
The sequence diagram (`docs/sequence-diagram.puml`) illustrates:
- **Test Setup**: Service virtualization initialization
- **Contract Testing**: API contract validation flow
- **End-to-End Testing**: Complete workflow execution
- **Cleanup**: Proper resource management

## Running the Tests

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Available ports 8081 and 8082 for WireMock services

### Quick Start
```bash
# Navigate to workspace
cd sd-microservicestesting-workspace

# Compile the project
mvn clean compile

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ServiceVirtualizationTest
mvn test -Dtest=ContractTest
mvn test -Dtest=EndToEndTest
```

### Test Execution Flow
1. **Setup Phase**: WireMock services start on ports 8081 and 8082
2. **Service Virtualization Tests**: Validate mock service behavior
3. **Contract Tests**: Verify API contracts and compatibility
4. **End-to-End Tests**: Execute complete business workflows
5. **Cleanup Phase**: Stop all virtual services

## Key Features

### Service Virtualization
- **Multiple Service Mocking**: Separate User and Order services
- **Scenario Coverage**: Success, error, and performance scenarios
- **Dynamic Responses**: Configurable responses based on request parameters
- **State Management**: Proper setup and teardown of mock services

### Contract Testing
- **Schema Validation**: Ensures response structure compliance
- **Version Compatibility**: Detects breaking changes between versions
- **Automated Generation**: Creates contracts from actual API interactions
- **Regression Prevention**: Validates backward compatibility

### End-to-End Testing
- **Workflow Orchestration**: Coordinates multi-service operations
- **Resilience Testing**: Validates error handling and recovery
- **Performance Validation**: Measures cross-service operation timing
- **Concurrent Testing**: Validates system behavior under concurrent load

## Best Practices Implemented

### 1. Test Isolation
- Each test class manages its own service virtualization
- Clean state between test methods
- Independent test execution

### 2. Error Handling
- Comprehensive exception handling in service clients
- Graceful degradation for service failures
- Proper error propagation and logging

### 3. Resource Management
- Automatic cleanup of WireMock services
- Proper thread management for concurrent tests
- Memory-efficient test execution

### 4. Maintainability
- Clear separation of concerns
- Reusable components across test scenarios
- Comprehensive logging and debugging support

## Integration with CI/CD

This framework is designed for CI/CD integration:
- **Fast Execution**: Virtual services eliminate external dependencies
- **Reliable Results**: Predictable responses ensure consistent test outcomes
- **Parallel Execution**: Tests can run in parallel for faster feedback
- **Environment Independence**: No external service dependencies

## Extension Points

### Adding New Services
1. Create new model classes for service entities
2. Implement service client with proper error handling
3. Add WireMock stubs in ServiceVirtualization
4. Create test scenarios for the new service

### Custom Contract Testing
1. Extend ContractTestBase for specific contract formats
2. Implement custom validation logic
3. Add contract generation for new service types

### Advanced Scenarios
1. Add chaos engineering tests for resilience
2. Implement load testing with multiple virtual services
3. Add security testing for service authentication
4. Create performance benchmarking suites

## Troubleshooting

### Common Issues
1. **Port Conflicts**: Ensure ports 8081 and 8082 are available
2. **Service Startup**: Check logs for WireMock initialization errors
3. **Test Timeouts**: Adjust timeout values for slow environments
4. **Memory Issues**: Increase JVM heap size for large test suites

### Debugging
- Enable debug logging in logback configuration
- Use WireMock request logging for API call verification
- Add breakpoints in service clients for step-through debugging

## Performance Characteristics

### Test Execution Times
- **Service Virtualization Tests**: ~2-5 seconds per test
- **Contract Tests**: ~1-3 seconds per test
- **End-to-End Tests**: ~5-15 seconds per test (including slow response simulation)

### Resource Usage
- **Memory**: ~100MB for WireMock services
- **CPU**: Minimal during test execution
- **Network**: No external network calls (all virtualized)

## Future Enhancements

### Planned Features
1. **Pact Integration**: Full consumer-driven contract testing
2. **Testcontainers**: Docker-based service testing
3. **Chaos Engineering**: Fault injection and resilience testing
4. **Performance Monitoring**: Detailed metrics and reporting
5. **Security Testing**: Authentication and authorization validation

This workspace provides a solid foundation for microservices testing and can be extended to meet specific project requirements while maintaining the core principles of isolation, reliability, and maintainability.
