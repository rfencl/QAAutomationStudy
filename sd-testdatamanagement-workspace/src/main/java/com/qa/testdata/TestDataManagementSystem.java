package com.qa.testdata;

import com.github.javafaker.Faker;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Minimal implementation of Test Data Management System.
 * Demonstrates core concepts with working code.
 */
public class TestDataManagementSystem {
    
    // Core Models
    public static class TestEntity {
        private String id;
        private String type;
        private String namespace;
        private Map<String, Object> attributes;
        private Instant createdAt;
        
        public TestEntity(String id, String type, String namespace, Map<String, Object> attributes, Instant createdAt) {
            this.id = id;
            this.type = type;
            this.namespace = namespace;
            this.attributes = attributes;
            this.createdAt = createdAt;
        }
        
        public static TestEntityBuilder builder() {
            return new TestEntityBuilder();
        }
        
        public String getId() { return id; }
        public String getType() { return type; }
        public String getNamespace() { return namespace; }
        public Map<String, Object> getAttributes() { return attributes; }
        public Instant getCreatedAt() { return createdAt; }
        
        @SuppressWarnings("unchecked")
        public <T> T getAttribute(String key) {
            return (T) attributes.get(key);
        }
        
        public static class TestEntityBuilder {
            private String id;
            private String type;
            private String namespace;
            private Map<String, Object> attributes;
            private Instant createdAt;
            
            public TestEntityBuilder id(String id) { this.id = id; return this; }
            public TestEntityBuilder type(String type) { this.type = type; return this; }
            public TestEntityBuilder namespace(String namespace) { this.namespace = namespace; return this; }
            public TestEntityBuilder attributes(Map<String, Object> attributes) { this.attributes = attributes; return this; }
            public TestEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            
            public TestEntity build() {
                return new TestEntity(id, type, namespace, attributes, createdAt);
            }
        }
    }
    
    public static class TestDataSet {
        private String id;
        private String templateId;
        private String namespace;
        private Map<String, List<TestEntity>> entities;
        private Instant createdAt;
        
        public TestDataSet(String id, String templateId, String namespace, Map<String, List<TestEntity>> entities, Instant createdAt) {
            this.id = id;
            this.templateId = templateId;
            this.namespace = namespace;
            this.entities = entities;
            this.createdAt = createdAt;
        }
        
        public static TestDataSetBuilder builder() {
            return new TestDataSetBuilder();
        }
        
        public String getId() { return id; }
        public String getTemplateId() { return templateId; }
        public String getNamespace() { return namespace; }
        public Map<String, List<TestEntity>> getEntities() { return entities; }
        public Instant getCreatedAt() { return createdAt; }
        
        public int getTotalEntityCount() {
            return entities.values().stream().mapToInt(List::size).sum();
        }
        
        public List<TestEntity> getEntitiesByType(String type) {
            return entities.getOrDefault(type, new ArrayList<>());
        }
        
        public static class TestDataSetBuilder {
            private String id;
            private String templateId;
            private String namespace;
            private Map<String, List<TestEntity>> entities;
            private Instant createdAt;
            
            public TestDataSetBuilder id(String id) { this.id = id; return this; }
            public TestDataSetBuilder templateId(String templateId) { this.templateId = templateId; return this; }
            public TestDataSetBuilder namespace(String namespace) { this.namespace = namespace; return this; }
            public TestDataSetBuilder entities(Map<String, List<TestEntity>> entities) { this.entities = entities; return this; }
            public TestDataSetBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
            
            public TestDataSet build() {
                return new TestDataSet(id, templateId, namespace, entities, createdAt);
            }
        }
    }
    
    public static class DataGenerationRequest {
        private String templateId;
        private String testExecutionId;
        private String environment;
        private Map<String, Object> parameters;
        
        public DataGenerationRequest(String templateId, String testExecutionId, String environment, Map<String, Object> parameters) {
            this.templateId = templateId;
            this.testExecutionId = testExecutionId;
            this.environment = environment;
            this.parameters = parameters;
        }
        
        public static DataGenerationRequestBuilder builder() {
            return new DataGenerationRequestBuilder();
        }
        
        public String getTemplateId() { return templateId; }
        public String getTestExecutionId() { return testExecutionId; }
        public String getEnvironment() { return environment; }
        public Map<String, Object> getParameters() { return parameters; }
        
        public static class DataGenerationRequestBuilder {
            private String templateId;
            private String testExecutionId;
            private String environment;
            private Map<String, Object> parameters;
            
            public DataGenerationRequestBuilder templateId(String templateId) { this.templateId = templateId; return this; }
            public DataGenerationRequestBuilder testExecutionId(String testExecutionId) { this.testExecutionId = testExecutionId; return this; }
            public DataGenerationRequestBuilder environment(String environment) { this.environment = environment; return this; }
            public DataGenerationRequestBuilder parameters(Map<String, Object> parameters) { this.parameters = parameters; return this; }
            
            public DataGenerationRequest build() {
                return new DataGenerationRequest(templateId, testExecutionId, environment, parameters);
            }
        }
    }
    
    // Core Services
    public static class TestDataManager {
        private final DataGenerator dataGenerator = new DataGenerator();
        private final IsolationManager isolationManager = new IsolationManager();
        private final ProvisioningRepository repository = new ProvisioningRepository();
        
        public TestDataSet generateAndProvision(DataGenerationRequest request) {
            String namespace = isolationManager.createNamespace(request.getTestExecutionId());
            
            TestDataSet dataSet = dataGenerator.generateDataSet(request, namespace);
            repository.storeDataSet(dataSet);
            
            return dataSet;
        }
        
        public TestEntity getEntity(String namespace, String entityType, String entityId) {
            return repository.getEntity(namespace, entityType, entityId);
        }
        
        public List<TestEntity> getEntitiesByType(String namespace, String entityType) {
            return repository.getEntitiesByType(namespace, entityType);
        }
        
        public void cleanup(String namespace) {
            repository.clearNamespace(namespace);
        }
    }
    
    public static class DataGenerator {
        private final Faker faker = new Faker();
        
        public TestDataSet generateDataSet(DataGenerationRequest request, String namespace) {
            Map<String, List<TestEntity>> entities = new HashMap<>();
            
            // Generate users
            List<TestEntity> users = generateUsers(5, namespace);
            entities.put("User", users);
            
            // Generate products
            List<TestEntity> products = generateProducts(10, namespace);
            entities.put("Product", products);
            
            // Generate orders
            List<TestEntity> orders = generateOrders(3, namespace);
            entities.put("Order", orders);
            
            return TestDataSet.builder()
                .id("ds_" + System.currentTimeMillis())
                .templateId(request.getTemplateId())
                .namespace(namespace)
                .entities(entities)
                .createdAt(Instant.now())
                .build();
        }
        
        private List<TestEntity> generateUsers(int count, String namespace) {
            List<TestEntity> users = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("firstName", faker.name().firstName());
                attributes.put("lastName", faker.name().lastName());
                attributes.put("email", faker.internet().emailAddress());
                attributes.put("status", "ACTIVE");
                
                TestEntity user = TestEntity.builder()
                    .id(namespace + "_user_" + i)
                    .type("User")
                    .namespace(namespace)
                    .attributes(attributes)
                    .createdAt(Instant.now())
                    .build();
                
                users.add(user);
            }
            return users;
        }
        
        private List<TestEntity> generateProducts(int count, String namespace) {
            List<TestEntity> products = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("name", faker.commerce().productName());
                attributes.put("price", faker.number().randomDouble(2, 10, 1000));
                attributes.put("category", faker.commerce().department());
                attributes.put("inStock", true);
                
                TestEntity product = TestEntity.builder()
                    .id(namespace + "_product_" + i)
                    .type("Product")
                    .namespace(namespace)
                    .attributes(attributes)
                    .createdAt(Instant.now())
                    .build();
                
                products.add(product);
            }
            return products;
        }
        
        private List<TestEntity> generateOrders(int count, String namespace) {
            List<TestEntity> orders = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("orderNumber", faker.number().digits(8));
                attributes.put("status", "PENDING");
                attributes.put("totalAmount", faker.number().randomDouble(2, 50, 500));
                
                TestEntity order = TestEntity.builder()
                    .id(namespace + "_order_" + i)
                    .type("Order")
                    .namespace(namespace)
                    .attributes(attributes)
                    .createdAt(Instant.now())
                    .build();
                
                orders.add(order);
            }
            return orders;
        }
    }
    
    public static class IsolationManager {
        private final ConcurrentMap<String, String> testExecutionToNamespace = new ConcurrentHashMap<>();
        
        public String createNamespace(String testExecutionId) {
            if (testExecutionId == null) {
                testExecutionId = "test_" + System.currentTimeMillis();
            }
            
            String namespace = "ns_" + testExecutionId + "_" + UUID.randomUUID().toString().substring(0, 8);
            testExecutionToNamespace.put(testExecutionId, namespace);
            
            return namespace;
        }
    }
    
    public static class ProvisioningRepository {
        private final ConcurrentMap<String, TestDataSet> dataSets = new ConcurrentHashMap<>();
        
        public void storeDataSet(TestDataSet dataSet) {
            dataSets.put(dataSet.getNamespace(), dataSet);
        }
        
        public TestEntity getEntity(String namespace, String entityType, String entityId) {
            TestDataSet dataSet = dataSets.get(namespace);
            if (dataSet == null) return null;
            
            List<TestEntity> entities = dataSet.getEntitiesByType(entityType);
            return entities.stream()
                .filter(entity -> entityId.equals(entity.getId()))
                .findFirst()
                .orElse(null);
        }
        
        public List<TestEntity> getEntitiesByType(String namespace, String entityType) {
            TestDataSet dataSet = dataSets.get(namespace);
            return dataSet != null ? dataSet.getEntitiesByType(entityType) : new ArrayList<>();
        }
        
        public void clearNamespace(String namespace) {
            dataSets.remove(namespace);
        }
    }
}
