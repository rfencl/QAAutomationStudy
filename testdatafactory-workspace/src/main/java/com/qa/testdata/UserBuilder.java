package com.qa.testdata;

import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserBuilder {
    private final User user = new User();
    private final Faker faker = new Faker();
    private final TestDataFactory factory;
    
    public UserBuilder(TestDataFactory factory) {
        this.factory = factory;
        // Set default values
        user.setId(UUID.randomUUID().toString());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
    }
    
    public UserBuilder withFirstName(String firstName) {
        user.setFirstName(firstName);
        return this;
    }
    
    public UserBuilder withLastName(String lastName) {
        user.setLastName(lastName);
        return this;
    }
    
    public UserBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }
    
    public UserBuilder withPhone(String phone) {
        user.setPhone(phone);
        return this;
    }
    
    public UserBuilder withStatus(String status) {
        user.setStatus(status);
        return this;
    }
    
    public UserBuilder withRandomData() {
        user.setFirstName(faker.name().firstName());
        user.setLastName(faker.name().lastName());
        user.setEmail(faker.internet().emailAddress());
        user.setPhone(faker.phoneNumber().phoneNumber());
        return this;
    }
    
    public User build() {
        validateUser();
        return user;
    }
    
    public User save() {
        User builtUser = build();
        return factory.save(builtUser);
    }
    
    private void validateUser() {
        if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
            throw new IllegalStateException("User firstName is required");
        }
        if (user.getLastName() == null || user.getLastName().isEmpty()) {
            throw new IllegalStateException("User lastName is required");
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new IllegalStateException("User email is required");
        }
    }
}
