-- QA Test Database Schema
-- This schema supports all the test scenarios from the study materials

-- Create database
CREATE DATABASE IF NOT EXISTS qa_test_db;
USE qa_test_db;

-- Users table
CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    registration_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_login DATETIME,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Customers table (for e-commerce scenarios)
CREATE TABLE Customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    registration_date DATE DEFAULT (CURRENT_DATE),
    status ENUM('active', 'inactive', 'suspended') DEFAULT 'active'
);

-- Orders table
CREATE TABLE Orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT,
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2),
    status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    shipping_address TEXT,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

-- Employees table (for salary queries)
CREATE TABLE Employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    department VARCHAR(50),
    salary DECIMAL(10, 2),
    hire_date DATE,
    manager_id INT,
    FOREIGN KEY (manager_id) REFERENCES Employees(employee_id)
);

-- Banking tables for money transfer scenarios
CREATE TABLE Accounts (
    account_id INT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    customer_id INT,
    account_type ENUM('checking', 'savings', 'credit') DEFAULT 'checking',
    balance DECIMAL(15, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

CREATE TABLE Transactions (
    transaction_id INT PRIMARY KEY AUTO_INCREMENT,
    from_account_id INT,
    to_account_id INT,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type ENUM('transfer', 'deposit', 'withdrawal') NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
    FOREIGN KEY (from_account_id) REFERENCES Accounts(account_id),
    FOREIGN KEY (to_account_id) REFERENCES Accounts(account_id)
);

-- Test data insertion
INSERT INTO Users (username, email, password_hash, first_name, last_name, registration_date) VALUES
('qa_user', 'qa@example.com', 'hashed_password_123', 'QA', 'User', DATE_SUB(NOW(), INTERVAL 3 DAY)),
('test_user1', 'test1@example.com', 'hashed_password_456', 'Test', 'User1', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('test_user2', 'test2@example.com', 'hashed_password_789', 'Test', 'User2', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('old_user', 'old@example.com', 'hashed_password_old', 'Old', 'User', DATE_SUB(NOW(), INTERVAL 30 DAY)),
('duplicate_email', 'duplicate@example.com', 'hash1', 'User', 'One', NOW()),
('duplicate_email2', 'duplicate@example.com', 'hash2', 'User', 'Two', NOW());

INSERT INTO Customers (name, email, phone, registration_date) VALUES
('John Doe', 'john@example.com', '123-456-7890', CURRENT_DATE),
('Jane Smith', 'jane@example.com', '098-765-4321', CURRENT_DATE),
('Bob Johnson', 'bob@example.com', '555-123-4567', DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)),
('Alice Brown', 'alice@example.com', '444-987-6543', CURRENT_DATE),
('Charlie Wilson', 'charlie@example.com', '333-555-7777', CURRENT_DATE);

INSERT INTO Orders (customer_id, total_amount, status) VALUES
(1, 150.00, 'delivered'),
(1, 200.00, 'shipped'),
(1, 75.50, 'delivered'),
(1, 300.00, 'processing'),
(1, 125.00, 'delivered'),
(1, 180.00, 'delivered'),
(2, 90.00, 'delivered'),
(2, 250.00, 'shipped'),
(3, 50.00, 'cancelled'),
(4, 400.00, 'delivered');

INSERT INTO Employees (first_name, last_name, email, department, salary, hire_date) VALUES
('John', 'Manager', 'john.manager@company.com', 'IT', 75000.00, '2020-01-15'),
('Sarah', 'Developer', 'sarah.dev@company.com', 'IT', 65000.00, '2021-03-10'),
('Mike', 'Tester', 'mike.test@company.com', 'QA', 55000.00, '2021-06-20'),
('Lisa', 'Senior Dev', 'lisa.senior@company.com', 'IT', 80000.00, '2019-08-05'),
('Tom', 'Junior Dev', 'tom.junior@company.com', 'IT', 45000.00, '2022-01-12'),
('Anna', 'QA Lead', 'anna.qa@company.com', 'QA', 70000.00, '2020-11-30');

INSERT INTO Accounts (account_number, customer_id, account_type, balance) VALUES
('ACC001', 1, 'checking', 5000.00),
('ACC002', 2, 'savings', 10000.00),
('ACC003', 3, 'checking', 2500.00),
('ACC004', 4, 'savings', 15000.00),
('ACC005', 5, 'checking', 3000.00);

-- Sample transactions
INSERT INTO Transactions (from_account_id, to_account_id, amount, transaction_type, description, status) VALUES
(1, 2, 500.00, 'transfer', 'Payment for services', 'completed'),
(2, 3, 200.00, 'transfer', 'Loan repayment', 'completed'),
(4, 1, 1000.00, 'transfer', 'Gift money', 'completed');
