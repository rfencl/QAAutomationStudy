# System Design 3: Scalable Test Data Management System

## Problem Statement
Design a comprehensive test data management system that handles test data generation, provisioning, isolation, cleanup, and versioning across multiple environments and test types while ensuring data privacy, consistency, and performance at scale.

## System Requirements

### Functional Requirements
1. **Data Generation**: Create realistic test data using templates and rules
2. **Data Provisioning**: Deploy data to multiple environments efficiently
3. **Data Isolation**: Ensure test data isolation between parallel executions
4. **Data Versioning**: Track and manage different versions of test datasets
5. **Data Cleanup**: Automatic cleanup of test data after execution
6. **Data Masking**: Anonymize sensitive production data for testing
7. **Data Relationships**: Maintain referential integrity across related entities
8. **Environment Sync**: Keep test data consistent across environments

### Non-Functional Requirements
1. **Scalability**: Support 1000+ concurrent test executions
2. **Performance**: Sub-second data provisioning for small datasets
3. **Reliability**: 99.9% data consistency and availability
4. **Security**: Secure handling of sensitive test data
5. **Compliance**: GDPR/CCPA compliant data handling

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        Test Data Management System                              │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐             │
│  │   Data          │    │   Data          │    │   Data          │             │
│  │   Generation    │    │   Provisioning  │    │   Cleanup       │             │
│  │   Service       │    │   Service       │    │   Service       │             │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘             │
│           │                       │                       │                     │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        Data Management Core                                │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐         │ │
│  │  │   Data      │ │   Version   │ │   Template  │ │   Isolation │         │ │
│  │  │   Catalog   │ │   Control   │ │   Engine    │ │   Manager   │         │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘         │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│           │                       │                       │                     │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐             │
│  │   Data          │    │   Environment   │    │   Security &    │             │
│  │   Storage       │    │   Manager       │    │   Compliance    │             │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Data Generation Service
```java
@Service
public class TestDataGenerationService {
    private final TemplateEngine templateEngine;
    private final DataGeneratorFactory generatorFactory;
    private final RelationshipManager relationshipManager;
    
    public TestDataSet generateDataSet(DataGenerationRequest request) {
        DataTemplate template = templateEngine.loadTemplate(request.getTemplateId());
        
        TestDataSet dataSet = TestDataSet.builder()
            .id(UUID.randomUUID().toString())
            .templateId(request.getTemplateId())
            .version(request.getVersion())
            .environment(request.getEnvironment())
            .build();
        
        // Generate entities in dependency order
        List<EntityTemplate> orderedTemplates = orderByDependencies(template.getEntityTemplates());
        
        for (EntityTemplate entityTemplate : orderedTemplates) {
            List<TestEntity> entities = generateEntities(entityTemplate, request.getCount());
            dataSet.addEntities(entityTemplate.getEntityType(), entities);
        }
        
        // Establish relationships
        relationshipManager.establishRelationships(dataSet, template.getRelationships());
        
        return dataSet;
    }
    
    private List<TestEntity> generateEntities(EntityTemplate template, int count) {
        DataGenerator generator = generatorFactory.getGenerator(template.getEntityType());
        
        return IntStream.range(0, count)
            .mapToObj(i -> generator.generate(template))
            .collect(Collectors.toList());
    }
}

@Component
public class UserDataGenerator implements DataGenerator<User> {
    private final FakerService fakerService;
    
    @Override
    public User generate(EntityTemplate template) {
        UserTemplate userTemplate = (UserTemplate) template;
        
        return User.builder()
            .id(generateId())
            .firstName(fakerService.name().firstName())
            .lastName(fakerService.name().lastName())
            .email(generateEmail(userTemplate.getEmailDomain()))
            .phone(fakerService.phoneNumber().phoneNumber())
            .dateOfBirth(generateDateOfBirth(userTemplate.getAgeRange()))
            .address(generateAddress())
            .status(selectRandomStatus(userTemplate.getAllowedStatuses()))
            .createdAt(generateCreatedDate(userTemplate.getCreationDateRange()))
            .build();
    }
    
    private String generateEmail(String domain) {
        String localPart = fakerService.internet().emailAddress().split("@")[0];
        return localPart + "@" + (domain != null ? domain : "example.com");
    }
}
```

### 2. Data Provisioning Service
```java
@Service
public class TestDataProvisioningService {
    private final Map<String, DataProvisioner> provisioners;
    private final DataIsolationManager isolationManager;
    private final DataVersionManager versionManager;
    
    public ProvisioningResult provisionDataSet(ProvisioningRequest request) {
        TestDataSet dataSet = loadDataSet(request.getDataSetId());
        
        // Create isolated namespace for this test execution
        String namespace = isolationManager.createNamespace(request.getTestExecutionId());
        
        ProvisioningResult result = ProvisioningResult.builder()
            .dataSetId(request.getDataSetId())
            .namespace(namespace)
            .environment(request.getEnvironment())
            .startTime(Instant.now())
            .build();
        
        try {
            // Provision data to target environment
            for (Map.Entry<String, List<TestEntity>> entry : dataSet.getEntities().entrySet()) {
                String entityType = entry.getKey();
                List<TestEntity> entities = entry.getValue();
                
                DataProvisioner provisioner = provisioners.get(entityType);
                ProvisioningMetrics metrics = provisioner.provision(entities, namespace, request.getEnvironment());
                
                result.addMetrics(entityType, metrics);
            }
            
            result.setStatus(ProvisioningStatus.SUCCESS);
            result.setEndTime(Instant.now());
            
        } catch (Exception e) {
            result.setStatus(ProvisioningStatus.FAILED);
            result.setError(e.getMessage());
            
            // Cleanup on failure
            cleanupNamespace(namespace, request.getEnvironment());
        }
        
        return result;
    }
}

@Component
public class DatabaseDataProvisioner implements DataProvisioner {
    private final Map<String, DataSource> dataSources;
    private final SqlGenerator sqlGenerator;
    
    @Override
    public ProvisioningMetrics provision(List<TestEntity> entities, String namespace, String environment) {
        DataSource dataSource = dataSources.get(environment);
        
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            
            for (TestEntity entity : entities) {
                try {
                    String sql = sqlGenerator.generateInsertSql(entity, namespace);
                    PreparedStatement statement = connection.prepareStatement(sql);
                    setStatementParameters(statement, entity);
                    statement.executeUpdate();
                    successCount++;
                } catch (SQLException e) {
                    failureCount++;
                    logProvisioningError(entity, e);
                }
            }
            
            connection.commit();
            
        } catch (SQLException e) {
            throw new ProvisioningException("Failed to provision data", e);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        return ProvisioningMetrics.builder()
            .totalEntities(entities.size())
            .successCount(successCount)
            .failureCount(failureCount)
            .executionTimeMs(executionTime)
            .build();
    }
}
```

### 3. Data Isolation Manager
```java
@Service
public class DataIsolationManager {
    private final NamespaceGenerator namespaceGenerator;
    private final IsolationStrategyFactory strategyFactory;
    
    public String createNamespace(String testExecutionId) {
        return namespaceGenerator.generateNamespace(testExecutionId);
    }
    
    public IsolatedDataContext createIsolatedContext(IsolationRequest request) {
        IsolationStrategy strategy = strategyFactory.getStrategy(request.getIsolationType());
        
        return strategy.createIsolatedContext(request);
    }
}

@Component
public class SchemaBasedIsolationStrategy implements IsolationStrategy {
    private final DatabaseManager databaseManager;
    
    @Override
    public IsolatedDataContext createIsolatedContext(IsolationRequest request) {
        String schemaName = "test_" + request.getNamespace();
        
        // Create isolated schema
        databaseManager.createSchema(schemaName, request.getEnvironment());
        
        // Copy base schema structure
        databaseManager.copySchemaStructure("base_schema", schemaName, request.getEnvironment());
        
        return IsolatedDataContext.builder()
            .namespace(request.getNamespace())
            .isolationType(IsolationType.SCHEMA_BASED)
            .connectionString(buildConnectionString(schemaName, request.getEnvironment()))
            .cleanupInstructions(createCleanupInstructions(schemaName))
            .build();
    }
}

@Component
public class PrefixBasedIsolationStrategy implements IsolationStrategy {
    @Override
    public IsolatedDataContext createIsolatedContext(IsolationRequest request) {
        String prefix = "test_" + request.getNamespace() + "_";
        
        return IsolatedDataContext.builder()
            .namespace(request.getNamespace())
            .isolationType(IsolationType.PREFIX_BASED)
            .tablePrefix(prefix)
            .cleanupInstructions(createPrefixCleanupInstructions(prefix))
            .build();
    }
}
```

### 4. Data Version Manager
```java
@Service
public class DataVersionManager {
    private final DataSetRepository dataSetRepository;
    private final VersioningStrategy versioningStrategy;
    
    public DataSetVersion createVersion(String dataSetId, VersionRequest request) {
        TestDataSet currentDataSet = dataSetRepository.findById(dataSetId);
        
        String newVersion = versioningStrategy.generateVersion(currentDataSet.getVersion(), request.getVersionType());
        
        TestDataSet versionedDataSet = currentDataSet.toBuilder()
            .version(newVersion)
            .parentVersion(currentDataSet.getVersion())
            .createdAt(Instant.now())
            .createdBy(request.getCreatedBy())
            .changeLog(request.getChangeLog())
            .build();
        
        dataSetRepository.save(versionedDataSet);
        
        return DataSetVersion.builder()
            .dataSetId(dataSetId)
            .version(newVersion)
            .parentVersion(currentDataSet.getVersion())
            .createdAt(Instant.now())
            .build();
    }
    
    public TestDataSet getVersion(String dataSetId, String version) {
        if ("latest".equals(version)) {
            return dataSetRepository.findLatestVersion(dataSetId);
        }
        
        return dataSetRepository.findByIdAndVersion(dataSetId, version);
    }
    
    public List<DataSetVersion> getVersionHistory(String dataSetId) {
        return dataSetRepository.findVersionHistory(dataSetId);
    }
}
```

### 5. Data Cleanup Service
```java
@Service
public class TestDataCleanupService {
    private final Map<String, DataCleaner> cleaners;
    private final CleanupScheduler cleanupScheduler;
    
    @EventListener
    public void handleTestCompletion(TestCompletionEvent event) {
        if (event.getCleanupPolicy() == CleanupPolicy.IMMEDIATE) {
            cleanupTestData(event.getNamespace(), event.getEnvironment());
        } else if (event.getCleanupPolicy() == CleanupPolicy.SCHEDULED) {
            scheduleCleanup(event.getNamespace(), event.getEnvironment(), event.getCleanupDelay());
        }
    }
    
    public void cleanupTestData(String namespace, String environment) {
        CleanupContext context = CleanupContext.builder()
            .namespace(namespace)
            .environment(environment)
            .startTime(Instant.now())
            .build();
        
        for (DataCleaner cleaner : cleaners.values()) {
            try {
                CleanupResult result = cleaner.cleanup(context);
                logCleanupResult(cleaner.getClass().getSimpleName(), result);
            } catch (Exception e) {
                logCleanupError(cleaner.getClass().getSimpleName(), e);
            }
        }
    }
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredData() {
        Instant cutoffTime = Instant.now().minus(Duration.ofHours(24));
        
        List<String> expiredNamespaces = findExpiredNamespaces(cutoffTime);
        
        for (String namespace : expiredNamespaces) {
            cleanupTestData(namespace, "all");
        }
    }
}

@Component
public class DatabaseDataCleaner implements DataCleaner {
    private final Map<String, DataSource> dataSources;
    
    @Override
    public CleanupResult cleanup(CleanupContext context) {
        DataSource dataSource = dataSources.get(context.getEnvironment());
        
        int deletedRecords = 0;
        List<String> errors = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Get all tables with test data for this namespace
            List<String> tables = getTablesWithTestData(connection, context.getNamespace());
            
            for (String table : tables) {
                try {
                    int deleted = deleteTestData(connection, table, context.getNamespace());
                    deletedRecords += deleted;
                } catch (SQLException e) {
                    errors.add("Failed to cleanup table " + table + ": " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            errors.add("Database connection failed: " + e.getMessage());
        }
        
        return CleanupResult.builder()
            .namespace(context.getNamespace())
            .deletedRecords(deletedRecords)
            .errors(errors)
            .executionTime(Duration.between(context.getStartTime(), Instant.now()))
            .build();
    }
}
```

## Data Models

### Test Data Set
```java
@Entity
@Table(name = "test_data_sets")
@Data
@Builder(toBuilder = true)
public class TestDataSet {
    @Id
    private String id;
    
    private String templateId;
    private String version;
    private String parentVersion;
    private String environment;
    
    @ElementCollection
    @CollectionTable(name = "test_data_entities")
    private Map<String, List<TestEntity>> entities;
    
    private Instant createdAt;
    private String createdBy;
    private String changeLog;
    
    @Enumerated(EnumType.STRING)
    private DataSetStatus status;
}
```

### Data Template
```java
@Entity
@Table(name = "data_templates")
@Data
@Builder
public class DataTemplate {
    @Id
    private String id;
    
    private String name;
    private String description;
    private String version;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<EntityTemplate> entityTemplates;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<RelationshipDefinition> relationships;
    
    @ElementCollection
    private Map<String, Object> globalParameters;
}
```

## Implementation Examples

### 1. Test Integration
```java
@TestMethodOrder(OrderAnnotation.class)
public class OrderProcessingTest {
    
    @Autowired
    private TestDataManager testDataManager;
    
    private String testNamespace;
    
    @BeforeEach
    public void setupTestData() {
        // Generate test data for this test
        DataGenerationRequest request = DataGenerationRequest.builder()
            .templateId("order-processing-template")
            .version("latest")
            .environment("test")
            .parameters(Map.of(
                "userCount", 5,
                "productCount", 10,
                "orderCount", 3
            ))
            .build();
        
        TestDataSet dataSet = testDataManager.generateAndProvision(request);
        testNamespace = dataSet.getNamespace();
    }
    
    @Test
    @Order(1)
    public void testCreateOrder() {
        // Test uses isolated test data
        User testUser = testDataManager.getEntity(testNamespace, "User", "test-user-1");
        Product testProduct = testDataManager.getEntity(testNamespace, "Product", "test-product-1");
        
        // Execute test logic
        Order order = orderService.createOrder(testUser.getId(), testProduct.getId());
        
        // Assertions
        assertNotNull(order.getId());
        assertEquals(testUser.getId(), order.getUserId());
    }
    
    @AfterEach
    public void cleanupTestData() {
        testDataManager.cleanup(testNamespace);
    }
}
```

### 2. Data Template Definition
```yaml
# order-processing-template.yml
id: order-processing-template
name: Order Processing Test Data
version: 1.0.0
description: Test data for order processing workflows

entities:
  - type: User
    count: ${userCount:5}
    template:
      firstName: ${faker.name.firstName}
      lastName: ${faker.name.lastName}
      email: ${faker.internet.emailAddress}
      status: ACTIVE
      createdAt: ${faker.date.past(365)}
      
  - type: Product
    count: ${productCount:10}
    template:
      name: ${faker.commerce.productName}
      price: ${faker.number.randomDouble(2, 10, 1000)}
      category: ${faker.commerce.department}
      inStock: true
      
  - type: Order
    count: ${orderCount:3}
    template:
      status: PENDING
      orderDate: ${faker.date.recent(30)}
      totalAmount: ${calculated.orderTotal}

relationships:
  - from: Order
    to: User
    type: MANY_TO_ONE
    foreignKey: userId
    
  - from: OrderItem
    to: Order
    type: MANY_TO_ONE
    foreignKey: orderId
    
  - from: OrderItem
    to: Product
    type: MANY_TO_ONE
    foreignKey: productId
```

### 3. Environment Configuration
```yaml
# application.yml
test-data-management:
  isolation:
    strategy: SCHEMA_BASED # or PREFIX_BASED, TENANT_BASED
    cleanup-policy: IMMEDIATE # or SCHEDULED, MANUAL
    
  environments:
    test:
      database:
        url: jdbc:postgresql://localhost:5432/testdb
        username: testuser
        password: testpass
      isolation:
        schema-prefix: test_
        
    staging:
      database:
        url: jdbc:postgresql://staging:5432/stagingdb
        username: staginguser
        password: stagingpass
      isolation:
        schema-prefix: staging_test_
        
  generation:
    faker:
      locale: en_US
      seed: 12345 # For reproducible data
      
  cleanup:
    retention-hours: 24
    batch-size: 1000
    max-concurrent-cleanups: 5
```

## Success Criteria
1. **Data Generation**: Generate 10,000+ entities per minute
2. **Provisioning Speed**: Provision small datasets (<1000 entities) in <5 seconds
3. **Isolation**: 100% data isolation between concurrent tests
4. **Cleanup Efficiency**: 99.9% successful cleanup rate
5. **Version Management**: Support for 100+ versions per dataset
6. **Scalability**: Support 1000+ concurrent test executions

## Security and Compliance
1. **Data Masking**: Automatic PII masking for production data
2. **Access Control**: Role-based access to test data
3. **Audit Trail**: Complete audit log of data operations
4. **Encryption**: Data encrypted at rest and in transit
5. **Retention Policies**: Automatic data deletion per compliance requirements

## Monitoring and Observability
1. **Metrics**: Data generation rates, provisioning times, cleanup success rates
2. **Alerting**: Alerts for failed operations and performance degradation
3. **Dashboards**: Real-time visibility into system health
4. **Logging**: Comprehensive logging for debugging and audit

## Extension Points
1. **Custom Generators**: Support for domain-specific data generators
2. **External Data Sources**: Integration with external data providers
3. **Cloud Storage**: Support for cloud-based data storage
4. **API Integration**: RESTful APIs for external system integration
5. **Machine Learning**: AI-powered test data generation
