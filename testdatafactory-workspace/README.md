# TestDataFactory Workspace - Builder Pattern Implementation

This workspace implements a robust test data factory using the Builder pattern for creating complex test data objects, as outlined in Practice_Problem_2_Test_Data_Factory.md.

## 🎯 Features Implemented

### Core Components
- **TestDataFactory** - Main factory class for creating and managing test data
- **UserBuilder** - Builder pattern implementation for User entities
- **OrderBuilder** - Builder pattern implementation for Order entities
- **Entity Classes** - User, Order, and OrderItem domain objects

### Key Features
- ✅ **Builder Pattern** - Fluent API for creating complex objects
- ✅ **Random Data Generation** - JavaFaker integration for realistic test data
- ✅ **Data Validation** - Built-in validation for required fields
- ✅ **Entity Tracking** - Automatic tracking of created entities for cleanup
- ✅ **Thread Safety** - ConcurrentHashMap for parallel test execution
- ✅ **Relationship Management** - Support for entity relationships (User -> Order)

## 🚀 Quick Start

### Run All Tests
```bash
cd testdatafactory-workspace
mvn clean test
```

### Run Specific Test Class
```bash
mvn test -Dtest=TestDataFactoryTest
```

## 📋 Usage Examples

### Basic User Creation
```java
TestDataFactory factory = new TestDataFactory();

// Create user with specific data
User user = factory.createUser()
    .withFirstName("John")
    .withLastName("Doe")
    .withEmail("john.doe@example.com")
    .withPhone("555-1234")
    .build();

// Create user with random data
User randomUser = factory.createUser()
    .withRandomData()
    .save(); // Automatically tracked for cleanup
```

### Order Creation with Relationships
```java
// Create user first
User customer = factory.createUser()
    .withRandomData()
    .save();

// Create order for the user
Order order = factory.createOrder()
    .forUser(customer)
    .withItems(3)
    .withStatus("CONFIRMED")
    .save();
```

### Bulk Data Generation
```java
// Generate multiple users
for (int i = 0; i < 10; i++) {
    factory.createUser()
        .withRandomData()
        .save();
}

// Get all created users
List<User> users = factory.getCreatedEntities(User.class);
```

## 🔧 Builder Pattern Benefits

### Fluent API
- **Readable Code** - Method chaining creates self-documenting code
- **Flexible Construction** - Build objects step by step
- **Default Values** - Sensible defaults with option to override

### Data Validation
- **Required Fields** - Automatic validation of mandatory fields
- **Business Rules** - Enforce business logic during construction
- **Early Failure** - Fail fast with clear error messages

### Test Data Management
- **Automatic Tracking** - All created entities are tracked
- **Easy Cleanup** - Single method to clean up all test data
- **Entity Relationships** - Support for complex object graphs

## 📊 Test Scenarios Covered

### TestDataFactoryTest.java
1. **testCreateUserWithSpecificData()** - Create user with explicit values
2. **testCreateUserWithRandomData()** - Generate user with faker data
3. **testSaveUser()** - Test entity tracking and retrieval
4. **testCreateOrderWithUser()** - Test entity relationships
5. **testBulkUserCreation()** - Generate multiple entities
6. **testOrderWithCustomAmount()** - Override calculated values
7. **testUserValidationFailure()** - Test validation rules
8. **testOrderValidationFailure()** - Test required field validation
9. **testCleanup()** - Test data cleanup functionality

### TestDataFactoryExampleTest.java
1. **testUserRegistrationScenario()** - Real-world user registration
2. **testOrderPlacementWorkflow()** - Complete order workflow
3. **testBulkDataGeneration()** - Performance testing data setup
4. **testDataRelationships()** - Complex entity relationships
5. **testDataTemplatePattern()** - Common data templates

## ⚙️ Configuration

### Default Values
```java
// User defaults
user.setId(UUID.randomUUID().toString());
user.setStatus("ACTIVE");
user.setCreatedAt(LocalDateTime.now());

// Order defaults
order.setId(UUID.randomUUID().toString());
order.setStatus("PENDING");
order.setOrderDate(LocalDateTime.now());
order.setItems(new ArrayList<>());
```

### Random Data Generation
- **Names** - Realistic first and last names
- **Email** - Valid email addresses
- **Phone** - Phone number formats
- **Products** - Commerce product names and prices
- **Amounts** - Random monetary values

## 📈 Expected Output

When running tests, you'll see entity creation and cleanup:
```
[TEST DATA] Saved User: User{id='123', firstName='John', lastName='Doe', email='john.doe@example.com', status='ACTIVE'}
[TEST DATA] Saved Order: Order{id='456', userId='123', totalAmount=89.97, status='CONFIRMED'}
[TEST DATA] Cleaning up 2 entities
[TEST DATA] Cleaning up 1 User entities
[TEST DATA] Cleaning up 1 Order entities
```

## 🎓 Learning Outcomes

This implementation demonstrates:
- **Builder Pattern** - Flexible object construction with fluent API
- **Factory Pattern** - Centralized object creation and management
- **Data Generation** - Realistic test data using JavaFaker
- **Entity Tracking** - Automatic cleanup and resource management
- **Validation** - Business rule enforcement during construction
- **Thread Safety** - Concurrent data structure usage

## 🔄 Extension Points

- Add more entity types (Product, Category, Payment)
- Implement data persistence (database integration)
- Add custom validation rules per entity
- Create data templates for common scenarios
- Add export/import functionality for test data sets
- Implement data relationships validation
