# Practice Problem 2: Design a Test Data Factory with Builder Pattern

## Problem Statement
Create a flexible test data factory system using the Builder pattern to generate consistent, realistic test data for automation tests. The system should support multiple data types, relationships, and customization options.

## Requirements

### Functional Requirements
1. **Builder Pattern Implementation**: Fluent API for data creation
2. **Multiple Data Types**: Support users, orders, products, addresses
3. **Data Relationships**: Handle foreign key relationships
4. **Randomization**: Generate realistic random data
5. **Customization**: Allow overriding specific fields
6. **Data Persistence**: Save to database or files
7. **Cleanup Support**: Track and clean up generated data

### Non-Functional Requirements
1. **Performance**: Fast data generation for large datasets
2. **Consistency**: Reproducible data with seed values
3. **Extensibility**: Easy to add new data types
4. **Thread Safety**: Support parallel test execution

## Technical Specifications

### Core Components
1. **TestDataFactory**: Main factory class
2. **Builder Classes**: Separate builders for each entity type
3. **DataGenerator**: Utility for random data generation
4. **DataRepository**: Handles data persistence and cleanup
5. **DataRelationshipManager**: Manages entity relationships

### Entity Models
```java
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Address address;
    private LocalDateTime createdAt;
}

public class Order {
    private Long id;
    private Long userId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime orderDate;
}
```

## Implementation Approach

### Step 1: Basic Builder Structure
```java
public class UserBuilder {
    private User user = new User();
    
    public UserBuilder withFirstName(String firstName) {
        user.setFirstName(firstName);
        return this;
    }
    
    public UserBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }
    
    public UserBuilder withRandomData() {
        user.setFirstName(DataGenerator.randomFirstName());
        user.setLastName(DataGenerator.randomLastName());
        user.setEmail(DataGenerator.randomEmail());
        return this;
    }
    
    public User build() {
        validateUser();
        return user;
    }
}
```

### Step 2: Factory Integration
```java
public class TestDataFactory {
    private DataRepository repository;
    private Set<Object> createdEntities = new HashSet<>();
    
    public UserBuilder createUser() {
        return new UserBuilder(this);
    }
    
    public OrderBuilder createOrder() {
        return new OrderBuilder(this);
    }
    
    public <T> T save(T entity) {
        T saved = repository.save(entity);
        createdEntities.add(saved);
        return saved;
    }
    
    public void cleanup() {
        createdEntities.forEach(repository::delete);
        createdEntities.clear();
    }
}
```

### Step 3: Advanced Features
```java
public class OrderBuilder {
    public OrderBuilder forUser(User user) {
        order.setUserId(user.getId());
        return this;
    }
    
    public OrderBuilder withItems(int count) {
        List<OrderItem> items = IntStream.range(0, count)
            .mapToObj(i -> createRandomOrderItem())
            .collect(Collectors.toList());
        order.setItems(items);
        return this;
    }
    
    public OrderBuilder withTotalAmount(BigDecimal amount) {
        order.setTotalAmount(amount);
        return this;
    }
}
```

## Example Usage Scenarios

### Scenario 1: Simple User Creation
```java
@Test
public void testUserRegistration() {
    User user = testDataFactory.createUser()
        .withFirstName("John")
        .withLastName("Doe")
        .withEmail("john.doe@example.com")
        .build();
    
    // Use user in test
    registrationPage.registerUser(user);
    
    // Cleanup handled automatically
}
```

### Scenario 2: Complex Order with Relationships
```java
@Test
public void testOrderPlacement() {
    User customer = testDataFactory.createUser()
        .withRandomData()
        .save();
    
    Order order = testDataFactory.createOrder()
        .forUser(customer)
        .withItems(3)
        .withStatus(OrderStatus.PENDING)
        .save();
    
    // Test order placement flow
    orderPage.placeOrder(order);
}
```

### Scenario 3: Bulk Data Generation
```java
@Test
public void testUserListPerformance() {
    List<User> users = IntStream.range(0, 100)
        .mapToObj(i -> testDataFactory.createUser()
            .withRandomData()
            .build())
        .collect(Collectors.toList());
    
    // Test with large dataset
    userListPage.loadUsers(users);
}
```

### Scenario 4: Data with Constraints
```java
@Test
public void testPremiumUserFeatures() {
    User premiumUser = testDataFactory.createUser()
        .withRandomData()
        .withAccountType(AccountType.PREMIUM)
        .withSubscriptionEndDate(LocalDate.now().plusYears(1))
        .save();
    
    // Test premium features
    dashboardPage.verifyPremiumFeatures(premiumUser);
}
```

## Advanced Features

### Data Templates
```java
public class DataTemplates {
    public static UserBuilder standardUser() {
        return new UserBuilder()
            .withRandomData()
            .withAccountType(AccountType.STANDARD)
            .withStatus(UserStatus.ACTIVE);
    }
    
    public static OrderBuilder completedOrder() {
        return new OrderBuilder()
            .withRandomItems(2, 5)
            .withStatus(OrderStatus.COMPLETED)
            .withOrderDate(LocalDateTime.now().minusDays(1));
    }
}
```

### Data Seeding
```java
@BeforeClass
public void seedTestData() {
    // Create base data for all tests
    adminUser = testDataFactory.createUser()
        .withRole(Role.ADMIN)
        .save();
    
    testProducts = testDataFactory.createProducts(10)
        .withCategory(Category.ELECTRONICS)
        .saveAll();
}
```

## Success Criteria
1. Fluent API allows readable test data creation
2. Supports complex entity relationships
3. Generates realistic random data
4. Automatic cleanup prevents data pollution
5. Thread-safe for parallel test execution
6. Easy to extend with new entity types
7. Performance suitable for large datasets

## Extension Points
1. **External Data Sources**: CSV, JSON, database templates
2. **Data Validation**: Built-in validation rules
3. **Localization**: Support for different locales
4. **Data Versioning**: Track data changes over time
5. **Integration**: REST API for data generation

## Testing Strategy
1. **Unit Tests**: Test individual builders and generators
2. **Integration Tests**: Verify database persistence
3. **Performance Tests**: Measure data generation speed
4. **Concurrency Tests**: Validate thread safety

## Deliverables
1. `TestDataFactory.java` - Main factory class
2. `UserBuilder.java`, `OrderBuilder.java` - Entity builders
3. `DataGenerator.java` - Random data utilities
4. `DataRepository.java` - Persistence layer
5. `DataTemplates.java` - Common data patterns
6. `TestDataFactoryTest.java` - Comprehensive tests
7. `README.md` - Usage guide and examples
