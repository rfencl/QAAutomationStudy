# Practice Problem 7: Implement a Page Object Factory with Reflection

## Problem Statement
Create a dynamic Page Object Factory that uses reflection to automatically instantiate page objects, inject WebDriver instances, initialize elements using annotations, and provide a clean API for page object management in Selenium automation frameworks.

## Requirements

### Functional Requirements
1. **Dynamic Instantiation**: Create page objects using reflection
2. **WebDriver Injection**: Automatically inject WebDriver instances
3. **Element Initialization**: Initialize @FindBy annotated elements
4. **Page Navigation**: Handle page transitions and validations
5. **Caching**: Cache page object instances for performance
6. **Validation**: Validate page objects are on correct page
7. **Error Handling**: Graceful handling of instantiation errors

### Non-Functional Requirements
1. **Performance**: Fast page object creation and caching
2. **Type Safety**: Compile-time type checking where possible
3. **Maintainability**: Easy to extend with new page objects
4. **Thread Safety**: Support parallel test execution

## Implementation Approach

### Page Object Factory
```java
@Component
public class PageObjectFactory {
    private final Map<Class<?>, Object> pageCache = new ConcurrentHashMap<>();
    private final WebDriver driver;
    
    @SuppressWarnings("unchecked")
    public <T> T getPage(Class<T> pageClass) {
        return (T) pageCache.computeIfAbsent(pageClass, this::createPageObject);
    }
    
    private <T> T createPageObject(Class<T> pageClass) {
        try {
            Constructor<T> constructor = pageClass.getDeclaredConstructor(WebDriver.class);
            T pageObject = constructor.newInstance(driver);
            
            // Initialize @FindBy elements
            PageFactory.initElements(driver, pageObject);
            
            // Validate page is loaded
            validatePage(pageObject);
            
            return pageObject;
        } catch (Exception e) {
            throw new PageObjectCreationException("Failed to create page object", e);
        }
    }
}
```

### Base Page Object
```java
public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    
    @PostConstruct
    public void validatePage() {
        if (!isPageLoaded()) {
            throw new WrongPageException("Expected page not loaded: " + getClass().getSimpleName());
        }
    }
    
    protected abstract boolean isPageLoaded();
    protected abstract String getPageUrl();
}
```

## Success Criteria
1. **Dynamic Creation**: Successfully create any page object class
2. **Element Initialization**: All @FindBy elements properly initialized
3. **Performance**: Page object creation under 10ms
4. **Validation**: Accurate page validation and error reporting
5. **Caching**: Efficient caching with proper cleanup

## Deliverables
1. `PageObjectFactory.java` - Main factory implementation
2. `BasePage.java` - Base page object class
3. `PageValidator.java` - Page validation utilities
4. `PageObjectFactoryTest.java` - Comprehensive tests
