# Database Test Cases
**Exercise: Write SQL test cases for a new record being added to a table**

## Test Case 1: Valid Record Insertion

### Test Case Information
- **Test Case ID**: TC_DB_001
- **Test Case Title**: Verify successful insertion of valid record into users table
- **Module/Feature**: Database - User Management
- **Priority**: High
- **Test Type**: Database Testing - CRUD Operations
- **Test Level**: Integration

### Prerequisites
- Database connection is established
- Users table exists with proper schema
- Required permissions for INSERT operations
- Test data is prepared

### Test Objective
Verify that a valid user record can be successfully inserted into the users table with all required fields.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| username | test_user_001 | Unique username |
| email | test001@example.com | Valid email format |
| password_hash | $2y$10$hashedpassword | Properly hashed password |
| first_name | John | Valid first name |
| last_name | Doe | Valid last name |
| date_of_birth | 1990-05-15 | Valid date format |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Connect to the database | Connection established successfully |
| 2 | Execute INSERT statement with valid data | Query executes without errors |
| 3 | Verify record was inserted using SELECT | Record exists with correct data |
| 4 | Check auto-generated fields (ID, timestamps) | Fields are populated correctly |
| 5 | Verify data integrity constraints | All constraints are satisfied |

### SQL Queries
```sql
-- Insert Statement
INSERT INTO users (username, email, password_hash, first_name, last_name, date_of_birth)
VALUES ('test_user_001', 'test001@example.com', '$2y$10$hashedpassword', 'John', 'Doe', '1990-05-15');

-- Verification Query
SELECT * FROM users WHERE username = 'test_user_001';

-- Count Verification
SELECT COUNT(*) FROM users WHERE email = 'test001@example.com';
```

### Expected Results
- INSERT statement executes successfully
- One record is added to the users table
- All field values match the inserted data
- Auto-generated user_id is assigned
- registration_date is set to current timestamp
- is_active defaults to TRUE

---

## Test Case 2: Duplicate Username Constraint

### Test Case Information
- **Test Case ID**: TC_DB_002
- **Test Case Title**: Verify unique constraint violation for duplicate username
- **Module/Feature**: Database - Data Integrity
- **Priority**: High
- **Test Type**: Database Testing - Constraint Validation
- **Test Level**: Integration

### Prerequisites
- Database connection is established
- Users table has UNIQUE constraint on username
- At least one existing user record

### Test Objective
Verify that attempting to insert a record with a duplicate username fails with appropriate constraint violation error.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| username | existing_user | Username that already exists |
| email | newemail@example.com | Different email address |
| password_hash | $2y$10$newhashedpassword | Valid password hash |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Verify existing user with target username | Record exists in database |
| 2 | Attempt to INSERT new record with same username | INSERT fails with constraint error |
| 3 | Verify error message indicates duplicate key | Error message is descriptive |
| 4 | Confirm no new record was created | Record count remains unchanged |
| 5 | Verify existing record is unaffected | Original record remains intact |

### SQL Queries
```sql
-- Check existing record
SELECT username FROM users WHERE username = 'existing_user';

-- Attempt duplicate insert (should fail)
INSERT INTO users (username, email, password_hash, first_name, last_name)
VALUES ('existing_user', 'newemail@example.com', '$2y$10$newhashedpassword', 'New', 'User');

-- Verify record count unchanged
SELECT COUNT(*) FROM users WHERE username = 'existing_user';
```

### Expected Results
- INSERT statement fails with unique constraint violation
- Error message indicates duplicate key violation
- No new record is created
- Existing record remains unchanged
- Database maintains data integrity

---

## Test Case 3: NULL Value Validation

### Test Case Information
- **Test Case ID**: TC_DB_003
- **Test Case Title**: Verify NOT NULL constraint enforcement
- **Module/Feature**: Database - Data Validation
- **Priority**: High
- **Test Type**: Database Testing - Constraint Validation
- **Test Level**: Integration

### Prerequisites
- Database connection is established
- Users table has NOT NULL constraints on required fields
- Understanding of table schema constraints

### Test Objective
Verify that attempting to insert NULL values in NOT NULL fields fails with appropriate constraint violation.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| username | NULL | NULL value for required field |
| email | test@example.com | Valid email |
| password_hash | $2y$10$hashedpassword | Valid password hash |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Attempt INSERT with NULL username | INSERT fails with NOT NULL constraint error |
| 2 | Attempt INSERT with NULL email | INSERT fails with NOT NULL constraint error |
| 3 | Attempt INSERT with NULL password_hash | INSERT fails with NOT NULL constraint error |
| 4 | Verify no records were created | Table record count unchanged |
| 5 | Test with empty string vs NULL | Validate different null-like values |

### SQL Queries
```sql
-- Test NULL username
INSERT INTO users (username, email, password_hash)
VALUES (NULL, 'test@example.com', '$2y$10$hashedpassword');

-- Test NULL email
INSERT INTO users (username, email, password_hash)
VALUES ('test_user', NULL, '$2y$10$hashedpassword');

-- Test NULL password
INSERT INTO users (username, email, password_hash)
VALUES ('test_user', 'test@example.com', NULL);

-- Verify no records created
SELECT COUNT(*) FROM users WHERE username IS NULL OR email IS NULL OR password_hash IS NULL;
```

### Expected Results
- All INSERT attempts with NULL required fields fail
- Appropriate NOT NULL constraint violation errors
- No partial records are created
- Database maintains referential integrity

---

## Test Case 4: Data Type Validation

### Test Case Information
- **Test Case ID**: TC_DB_004
- **Test Case Title**: Verify data type constraint enforcement
- **Module/Feature**: Database - Data Type Validation
- **Priority**: Medium
- **Test Type**: Database Testing - Data Validation
- **Test Level**: Integration

### Prerequisites
- Database connection is established
- Understanding of column data types and constraints
- Test data with invalid data types

### Test Objective
Verify that inserting data with incorrect data types fails with appropriate type conversion errors.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| date_of_birth | 'invalid-date' | Invalid date format |
| registration_date | 'not-a-timestamp' | Invalid timestamp |
| is_active | 'not-boolean' | Invalid boolean value |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Attempt INSERT with invalid date format | INSERT fails with date conversion error |
| 2 | Attempt INSERT with invalid timestamp | INSERT fails with timestamp error |
| 3 | Attempt INSERT with invalid boolean | INSERT fails with boolean conversion error |
| 4 | Test boundary values for numeric fields | Validate numeric constraints |
| 5 | Test string length limits | Validate VARCHAR constraints |

### SQL Queries
```sql
-- Test invalid date
INSERT INTO users (username, email, password_hash, date_of_birth)
VALUES ('test_user', 'test@example.com', 'hash', 'invalid-date');

-- Test string length limit
INSERT INTO users (username, email, password_hash)
VALUES ('very_long_username_that_exceeds_column_limit_of_fifty_characters', 'test@example.com', 'hash');

-- Test valid boundary values
INSERT INTO users (username, email, password_hash, date_of_birth)
VALUES ('u', 'a@b.co', 'h', '1900-01-01');
```

### Expected Results
- Invalid data type insertions fail with type conversion errors
- String length violations are caught and rejected
- Boundary values within limits are accepted
- Database maintains data type integrity

---

## Test Case 5: Foreign Key Constraint Validation

### Test Case Information
- **Test Case ID**: TC_DB_005
- **Test Case Title**: Verify foreign key constraint enforcement in orders table
- **Module/Feature**: Database - Referential Integrity
- **Priority**: High
- **Test Type**: Database Testing - Referential Integrity
- **Test Level**: Integration

### Prerequisites
- Database connection is established
- Orders table has foreign key reference to users table
- At least one valid user record exists

### Test Objective
Verify that foreign key constraints are properly enforced when inserting records with references to other tables.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| user_id | 999999 | Non-existent user ID |
| total_amount | 100.00 | Valid order amount |
| order_status | pending | Valid status |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Verify non-existent user_id doesn't exist | No record found with ID 999999 |
| 2 | Attempt INSERT order with invalid user_id | INSERT fails with foreign key error |
| 3 | Insert order with valid user_id | INSERT succeeds |
| 4 | Verify referential integrity maintained | Order references valid user |
| 5 | Test cascading delete behavior | Verify cascade rules work correctly |

### SQL Queries
```sql
-- Verify user doesn't exist
SELECT COUNT(*) FROM users WHERE user_id = 999999;

-- Attempt insert with invalid foreign key (should fail)
INSERT INTO orders (user_id, total_amount, order_status)
VALUES (999999, 100.00, 'pending');

-- Insert with valid foreign key (should succeed)
INSERT INTO orders (user_id, total_amount, order_status)
VALUES (1, 100.00, 'pending');

-- Verify relationship
SELECT o.order_id, o.user_id, u.username
FROM orders o
JOIN users u ON o.user_id = u.user_id
WHERE o.user_id = 1;
```

### Expected Results
- INSERT with invalid foreign key fails
- Foreign key constraint violation error is returned
- INSERT with valid foreign key succeeds
- Referential integrity is maintained
- JOIN queries work correctly

---

## Test Case 6: Transaction Rollback Testing

### Test Case Information
- **Test Case ID**: TC_DB_006
- **Test Case Title**: Verify transaction rollback on error conditions
- **Module/Feature**: Database - Transaction Management
- **Priority**: High
- **Test Type**: Database Testing - Transaction Integrity
- **Test Level**: Integration

### Prerequisites
- Database supports transactions
- Multiple related tables for testing
- Understanding of ACID properties

### Test Objective
Verify that database transactions are properly rolled back when errors occur, maintaining data consistency.

### Test Data
| Operation | Table | Data | Description |
|-----------|-------|------|-------------|
| INSERT | users | Valid user data | First operation |
| INSERT | orders | Invalid foreign key | Second operation (should fail) |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | BEGIN TRANSACTION | Transaction started |
| 2 | INSERT valid user record | User inserted successfully |
| 3 | INSERT order with invalid user_id | INSERT fails |
| 4 | ROLLBACK TRANSACTION | Transaction rolled back |
| 5 | Verify no records were committed | Both operations undone |

### SQL Queries
```sql
-- Start transaction
BEGIN TRANSACTION;

-- Insert user (should succeed)
INSERT INTO users (username, email, password_hash)
VALUES ('transaction_test', 'trans@test.com', 'hash');

-- Get the user_id for verification
SELECT user_id FROM users WHERE username = 'transaction_test';

-- Insert order with invalid user_id (should fail)
INSERT INTO orders (user_id, total_amount)
VALUES (999999, 100.00);

-- Rollback due to error
ROLLBACK;

-- Verify no records exist
SELECT COUNT(*) FROM users WHERE username = 'transaction_test';
SELECT COUNT(*) FROM orders WHERE user_id = 999999;
```

### Expected Results
- Transaction begins successfully
- First INSERT succeeds within transaction
- Second INSERT fails due to constraint violation
- ROLLBACK undoes all changes in transaction
- No partial data remains in database

---

## Test Case 7: Concurrent Access Testing

### Test Case Information
- **Test Case ID**: TC_DB_007
- **Test Case Title**: Verify database behavior under concurrent access
- **Module/Feature**: Database - Concurrency Control
- **Priority**: Medium
- **Test Type**: Database Testing - Concurrency
- **Test Level**: Integration

### Prerequisites
- Database supports concurrent connections
- Multiple database connections available
- Understanding of locking mechanisms

### Test Objective
Verify that the database properly handles concurrent INSERT operations and maintains data integrity.

### Test Data
| Connection | Username | Email | Description |
|------------|----------|-------|-------------|
| Connection 1 | concurrent_user_1 | user1@test.com | First concurrent insert |
| Connection 2 | concurrent_user_2 | user2@test.com | Second concurrent insert |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Open two database connections | Both connections established |
| 2 | Simultaneously INSERT different records | Both inserts succeed |
| 3 | Simultaneously INSERT same username | One succeeds, one fails |
| 4 | Verify data consistency | Database state is consistent |
| 5 | Test deadlock scenarios | Deadlocks are handled properly |

### SQL Queries
```sql
-- Connection 1
INSERT INTO users (username, email, password_hash)
VALUES ('concurrent_user_1', 'user1@test.com', 'hash1');

-- Connection 2 (simultaneous)
INSERT INTO users (username, email, password_hash)
VALUES ('concurrent_user_2', 'user2@test.com', 'hash2');

-- Verification
SELECT username, email FROM users 
WHERE username IN ('concurrent_user_1', 'concurrent_user_2')
ORDER BY username;
```

### Expected Results
- Concurrent inserts with different data succeed
- Concurrent inserts with duplicate keys handled correctly
- Database maintains consistency under concurrent load
- Proper locking prevents data corruption

---

## Test Case 8: Performance Testing for Bulk Inserts

### Test Case Information
- **Test Case ID**: TC_DB_008
- **Test Case Title**: Verify performance of bulk INSERT operations
- **Module/Feature**: Database - Performance
- **Priority**: Medium
- **Test Type**: Database Testing - Performance
- **Test Level**: System

### Prerequisites
- Database connection is established
- Performance monitoring tools available
- Large dataset for bulk operations

### Test Objective
Verify that bulk INSERT operations complete within acceptable time limits and don't degrade system performance.

### Test Data
| Parameter | Value | Description |
|-----------|-------|-------------|
| Record Count | 10,000 | Number of records to insert |
| Batch Size | 1,000 | Records per batch |
| Max Time | 30 seconds | Maximum acceptable time |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Prepare bulk test data | 10,000 unique records ready |
| 2 | Record start time | Baseline timestamp captured |
| 3 | Execute bulk INSERT operation | All records inserted successfully |
| 4 | Record end time | Completion timestamp captured |
| 5 | Verify all records inserted | Count matches expected |
| 6 | Check system performance impact | System remains responsive |

### SQL Queries
```sql
-- Bulk insert using batch approach
INSERT INTO users (username, email, password_hash, first_name, last_name)
VALUES 
('bulk_user_1', 'bulk1@test.com', 'hash1', 'User', '1'),
('bulk_user_2', 'bulk2@test.com', 'hash2', 'User', '2'),
-- ... continue for batch size
('bulk_user_1000', 'bulk1000@test.com', 'hash1000', 'User', '1000');

-- Verification query
SELECT COUNT(*) FROM users WHERE username LIKE 'bulk_user_%';

-- Performance analysis
SELECT 
    COUNT(*) as total_records,
    MIN(registration_date) as first_insert,
    MAX(registration_date) as last_insert,
    TIMESTAMPDIFF(SECOND, MIN(registration_date), MAX(registration_date)) as duration_seconds
FROM users 
WHERE username LIKE 'bulk_user_%';
```

### Expected Results
- Bulk INSERT completes within time limit
- All records are inserted successfully
- System performance remains acceptable
- Database indexes are updated correctly
- No memory or connection issues occur
