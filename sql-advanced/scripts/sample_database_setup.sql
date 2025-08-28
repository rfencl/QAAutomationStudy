-- Sample Database Setup for QA Testing
-- Exercise: Write queries to validate application data against DB records

-- Create database (uncomment if needed)
-- CREATE DATABASE qa_test_db;
-- USE qa_test_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    date_of_birth DATE,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    user_type ENUM('customer', 'admin', 'moderator') DEFAULT 'customer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Products table
CREATE TABLE IF NOT EXISTS products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category_id INT,
    stock_quantity INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    parent_category_id INT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Orders table
CREATE TABLE IF NOT EXISTS orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_status ENUM('pending', 'processing', 'shipped', 'delivered', 'cancelled') DEFAULT 'pending',
    shipping_address TEXT,
    billing_address TEXT,
    payment_method VARCHAR(50),
    payment_status ENUM('pending', 'completed', 'failed', 'refunded') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Order Items table
CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- User Sessions table (for login tracking)
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_approved BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- Add foreign key constraints
ALTER TABLE products ADD FOREIGN KEY (category_id) REFERENCES categories(category_id);
ALTER TABLE categories ADD FOREIGN KEY (parent_category_id) REFERENCES categories(category_id);

-- Insert sample data for testing

-- Categories
INSERT INTO categories (category_name, description) VALUES
('Electronics', 'Electronic devices and accessories'),
('Books', 'Books and educational materials'),
('Clothing', 'Apparel and fashion items'),
('Home & Garden', 'Home improvement and garden supplies'),
('Sports', 'Sports equipment and accessories');

-- Products
INSERT INTO products (product_name, description, price, category_id, stock_quantity) VALUES
('Laptop Computer', 'High-performance laptop for work and gaming', 999.99, 1, 50),
('Smartphone', 'Latest model smartphone with advanced features', 699.99, 1, 100),
('Programming Book', 'Learn advanced programming concepts', 49.99, 2, 200),
('T-Shirt', 'Comfortable cotton t-shirt', 19.99, 3, 500),
('Garden Tools Set', 'Complete set of garden tools', 89.99, 4, 75),
('Tennis Racket', 'Professional tennis racket', 129.99, 5, 30);

-- Users (passwords are hashed versions of 'password123')
INSERT INTO users (username, email, password_hash, first_name, last_name, date_of_birth, user_type) VALUES
('john_doe', 'john.doe@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Doe', '1990-05-15', 'customer'),
('jane_smith', 'jane.smith@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Jane', 'Smith', '1985-08-22', 'customer'),
('admin_user', 'admin@company.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin', 'User', '1980-01-01', 'admin'),
('qa_tester', 'qa.tester@company.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'QA', 'Tester', '1992-03-10', 'customer'),
('test_user', 'test.user@email.com', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Test', 'User', '1988-12-05', 'customer');

-- Orders
INSERT INTO orders (user_id, total_amount, order_status, shipping_address, payment_method, payment_status) VALUES
(1, 1049.98, 'delivered', '123 Main St, City, State 12345', 'credit_card', 'completed'),
(2, 699.99, 'shipped', '456 Oak Ave, City, State 12345', 'paypal', 'completed'),
(1, 169.98, 'processing', '123 Main St, City, State 12345', 'credit_card', 'completed'),
(4, 49.99, 'delivered', '789 Pine St, City, State 12345', 'debit_card', 'completed'),
(5, 219.98, 'pending', '321 Elm St, City, State 12345', 'credit_card', 'pending');

-- Order Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) VALUES
(1, 1, 1, 999.99, 999.99),
(1, 3, 1, 49.99, 49.99),
(2, 2, 1, 699.99, 699.99),
(3, 4, 2, 19.99, 39.98),
(3, 6, 1, 129.99, 129.99),
(4, 3, 1, 49.99, 49.99),
(5, 5, 1, 89.99, 89.99),
(5, 6, 1, 129.99, 129.99);

-- User Sessions (some recent logins)
INSERT INTO user_sessions (user_id, session_token, login_time, ip_address, user_agent) VALUES
(1, 'session_token_123', NOW() - INTERVAL 2 HOUR, '192.168.1.100', 'Mozilla/5.0 Chrome/91.0'),
(2, 'session_token_456', NOW() - INTERVAL 1 DAY, '192.168.1.101', 'Mozilla/5.0 Firefox/89.0'),
(4, 'session_token_789', NOW() - INTERVAL 5 HOUR, '192.168.1.102', 'Mozilla/5.0 Safari/14.0'),
(1, 'session_token_abc', NOW() - INTERVAL 10 DAY, '192.168.1.100', 'Mozilla/5.0 Chrome/90.0'),
(5, 'session_token_def', NOW() - INTERVAL 3 DAY, '192.168.1.103', 'Mozilla/5.0 Edge/91.0');

-- Reviews
INSERT INTO reviews (user_id, product_id, rating, review_text, is_approved) VALUES
(1, 1, 5, 'Excellent laptop, very fast and reliable!', TRUE),
(2, 2, 4, 'Great smartphone, good value for money.', TRUE),
(4, 3, 5, 'Very informative book, learned a lot!', TRUE),
(1, 4, 3, 'T-shirt quality is okay, nothing special.', TRUE),
(5, 5, 4, 'Good garden tools, sturdy and well-made.', FALSE);

-- Update last_login for some users
UPDATE users SET last_login = NOW() - INTERVAL 2 HOUR WHERE user_id = 1;
UPDATE users SET last_login = NOW() - INTERVAL 1 DAY WHERE user_id = 2;
UPDATE users SET last_login = NOW() - INTERVAL 5 HOUR WHERE user_id = 4;
UPDATE users SET last_login = NOW() - INTERVAL 3 DAY WHERE user_id = 5;

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_registration_date ON users(registration_date);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_order_date ON orders(order_date);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_sessions_login_time ON user_sessions(login_time);

-- Create views for common queries
CREATE VIEW active_users AS
SELECT user_id, username, email, first_name, last_name, registration_date, last_login
FROM users 
WHERE is_active = TRUE;

CREATE VIEW order_summary AS
SELECT 
    o.order_id,
    u.username,
    u.email,
    o.order_date,
    o.total_amount,
    o.order_status,
    o.payment_status,
    COUNT(oi.order_item_id) as item_count
FROM orders o
JOIN users u ON o.user_id = u.user_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id, u.username, u.email, o.order_date, o.total_amount, o.order_status, o.payment_status;

CREATE VIEW product_stats AS
SELECT 
    p.product_id,
    p.product_name,
    c.category_name,
    p.price,
    p.stock_quantity,
    COALESCE(AVG(r.rating), 0) as avg_rating,
    COUNT(r.review_id) as review_count,
    COALESCE(SUM(oi.quantity), 0) as total_sold
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
LEFT JOIN reviews r ON p.product_id = r.product_id AND r.is_approved = TRUE
LEFT JOIN order_items oi ON p.product_id = oi.product_id
GROUP BY p.product_id, p.product_name, c.category_name, p.price, p.stock_quantity;
