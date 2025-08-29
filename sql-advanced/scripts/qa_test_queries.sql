-- QA Test Queries for Database Validation
-- Exercise: Write queries to validate application data against DB records

-- =============================================================================
-- 1. USER REGISTRATION AND LOGIN VALIDATION QUERIES
-- =============================================================================

-- Exercise: Write a query to find all users who registered in the past 30 days
SELECT 
    user_id,
    username,
    email,
    first_name,
    last_name,
    registration_date,
    DATEDIFF(CURRENT_DATE, DATE(registration_date)) as days_since_registration
FROM users 
WHERE registration_date >= DATE_SUB(CURRENT_DATE, INTERVAL 30 DAY)
ORDER BY registration_date DESC;

-- Validate user login functionality
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.last_login,
    COUNT(s.session_id) as active_sessions,
    MAX(s.login_time) as latest_session
FROM users u
LEFT JOIN user_sessions s ON u.user_id = s.user_id AND s.is_active = TRUE
WHERE u.username = 'john_doe'  -- Replace with actual username being tested
GROUP BY u.user_id, u.username, u.email, u.last_login;

-- Check for duplicate usernames or emails (data integrity)
SELECT 'username' as field_type, username as value, COUNT(*) as count
FROM users 
GROUP BY username 
HAVING COUNT(*) > 1
UNION ALL
SELECT 'email' as field_type, email as value, COUNT(*) as count
FROM users 
GROUP BY email 
HAVING COUNT(*) > 1;

-- Validate password reset functionality (check session cleanup)
SELECT 
    user_id,
    COUNT(*) as total_sessions,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_sessions,
    SUM(CASE WHEN logout_time IS NOT NULL THEN 1 ELSE 0 END) as logged_out_sessions
FROM user_sessions 
WHERE user_id = 1  -- Replace with actual user_id
GROUP BY user_id;

-- =============================================================================
-- 2. ORDER PROCESSING VALIDATION QUERIES
-- =============================================================================

-- Exercise: Validate that an order placed on the UI is reflected in the Orders table
SELECT 
    o.order_id,
    o.user_id,
    u.username,
    o.order_date,
    o.total_amount,
    o.order_status,
    o.payment_status,
    COUNT(oi.order_item_id) as item_count,
    SUM(oi.total_price) as calculated_total
FROM orders o
JOIN users u ON o.user_id = u.user_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
WHERE o.order_id = 1  -- Replace with actual order_id being tested
GROUP BY o.order_id, o.user_id, u.username, o.order_date, o.total_amount, o.order_status, o.payment_status;

-- Validate order total calculation
SELECT 
    o.order_id,
    o.total_amount as stored_total,
    SUM(oi.total_price) as calculated_total,
    CASE 
        WHEN ABS(o.total_amount - SUM(oi.total_price)) < 0.01 THEN 'PASS'
        ELSE 'FAIL'
    END as validation_result
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY o.order_id, o.total_amount
HAVING validation_result = 'FAIL';  -- Only show failed validations

-- Check inventory updates after order placement
SELECT 
    p.product_id,
    p.product_name,
    p.stock_quantity as current_stock,
    COALESCE(SUM(oi.quantity), 0) as total_ordered,
    p.stock_quantity + COALESCE(SUM(oi.quantity), 0) as expected_original_stock
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id 
WHERE o.order_status IN ('processing', 'shipped', 'delivered')
GROUP BY p.product_id, p.product_name, p.stock_quantity;

-- Validate order status transitions
SELECT 
    order_id,
    order_status,
    payment_status,
    order_date,
    updated_at,
    CASE 
        WHEN order_status = 'pending' AND payment_status = 'pending' THEN 'Valid'
        WHEN order_status = 'processing' AND payment_status = 'completed' THEN 'Valid'
        WHEN order_status = 'shipped' AND payment_status = 'completed' THEN 'Valid'
        WHEN order_status = 'delivered' AND payment_status = 'completed' THEN 'Valid'
        WHEN order_status = 'cancelled' THEN 'Valid'
        ELSE 'Invalid Status Combination'
    END as status_validation
FROM orders
WHERE order_id = 1;  -- Replace with actual order_id

-- =============================================================================
-- 3. PRODUCT AND INVENTORY VALIDATION QUERIES
-- =============================================================================

-- Validate product creation and updates
SELECT 
    product_id,
    product_name,
    price,
    stock_quantity,
    is_active,
    created_at,
    updated_at,
    CASE 
        WHEN price <= 0 THEN 'Invalid Price'
        WHEN stock_quantity < 0 THEN 'Invalid Stock'
        WHEN product_name IS NULL OR product_name = '' THEN 'Invalid Name'
        ELSE 'Valid'
    END as validation_status
FROM products
WHERE product_id = 1;  -- Replace with actual product_id

-- Check for products with low stock
SELECT 
    product_id,
    product_name,
    stock_quantity,
    COALESCE(SUM(oi.quantity), 0) as pending_orders
FROM products p
LEFT JOIN order_items oi ON p.product_id = oi.product_id
LEFT JOIN orders o ON oi.order_id = o.order_id AND o.order_status = 'pending'
WHERE p.stock_quantity < 10  -- Low stock threshold
GROUP BY p.product_id, p.product_name, p.stock_quantity;

-- =============================================================================
-- 4. SEARCH AND FILTERING VALIDATION QUERIES
-- =============================================================================

-- Validate search functionality
SELECT 
    p.product_id,
    p.product_name,
    p.description,
    c.category_name,
    p.price,
    p.stock_quantity
FROM products p
JOIN categories c ON p.category_id = c.category_id
WHERE p.is_active = TRUE
  AND (p.product_name LIKE '%laptop%' OR p.description LIKE '%laptop%')  -- Replace with search term
ORDER BY p.product_name;

-- Validate category filtering
SELECT 
    p.product_id,
    p.product_name,
    c.category_name,
    p.price
FROM products p
JOIN categories c ON p.category_id = c.category_id
WHERE c.category_name = 'Electronics'  -- Replace with actual category
  AND p.is_active = TRUE
ORDER BY p.price;

-- Validate price range filtering
SELECT 
    product_id,
    product_name,
    price,
    stock_quantity
FROM products
WHERE price BETWEEN 50.00 AND 200.00  -- Replace with actual price range
  AND is_active = TRUE
  AND stock_quantity > 0
ORDER BY price;

-- =============================================================================
-- 5. USER ACTIVITY AND SESSION VALIDATION QUERIES
-- =============================================================================

-- Validate user session management
SELECT 
    s.session_id,
    s.user_id,
    u.username,
    s.login_time,
    s.logout_time,
    s.is_active,
    TIMESTAMPDIFF(MINUTE, s.login_time, COALESCE(s.logout_time, NOW())) as session_duration_minutes
FROM user_sessions s
JOIN users u ON s.user_id = u.user_id
WHERE s.user_id = 1  -- Replace with actual user_id
ORDER BY s.login_time DESC;

-- Check for concurrent sessions (potential security issue)
SELECT 
    user_id,
    COUNT(*) as concurrent_sessions,
    GROUP_CONCAT(session_id) as session_ids
FROM user_sessions
WHERE is_active = TRUE
GROUP BY user_id
HAVING COUNT(*) > 1;

-- =============================================================================
-- 6. DATA INTEGRITY AND RELATIONSHIP VALIDATION QUERIES
-- =============================================================================

-- Exercise: Practice combining multiple joins to test relational integrity
SELECT 
    u.user_id,
    u.username,
    COUNT(DISTINCT o.order_id) as total_orders,
    COUNT(DISTINCT oi.order_item_id) as total_items,
    SUM(o.total_amount) as total_spent,
    AVG(r.rating) as avg_rating_given,
    COUNT(DISTINCT r.review_id) as reviews_written
FROM users u
LEFT JOIN orders o ON u.user_id = o.user_id
LEFT JOIN order_items oi ON o.order_id = oi.order_id
LEFT JOIN reviews r ON u.user_id = r.user_id
WHERE u.is_active = TRUE
GROUP BY u.user_id, u.username
ORDER BY total_spent DESC;

-- Validate foreign key relationships
SELECT 'orders_without_users' as issue_type, COUNT(*) as count
FROM orders o
LEFT JOIN users u ON o.user_id = u.user_id
WHERE u.user_id IS NULL
UNION ALL
SELECT 'order_items_without_orders' as issue_type, COUNT(*) as count
FROM order_items oi
LEFT JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_id IS NULL
UNION ALL
SELECT 'order_items_without_products' as issue_type, COUNT(*) as count
FROM order_items oi
LEFT JOIN products p ON oi.product_id = p.product_id
WHERE p.product_id IS NULL;

-- =============================================================================
-- 7. PERFORMANCE AND ANALYTICS VALIDATION QUERIES
-- =============================================================================

-- Validate reporting data
SELECT 
    DATE(o.order_date) as order_date,
    COUNT(*) as orders_count,
    SUM(o.total_amount) as daily_revenue,
    AVG(o.total_amount) as avg_order_value,
    COUNT(DISTINCT o.user_id) as unique_customers
FROM orders o
WHERE o.order_date >= DATE_SUB(CURRENT_DATE, INTERVAL 7 DAY)
GROUP BY DATE(o.order_date)
ORDER BY order_date DESC;

-- Top selling products validation
SELECT 
    p.product_id,
    p.product_name,
    SUM(oi.quantity) as total_sold,
    SUM(oi.total_price) as total_revenue,
    COUNT(DISTINCT oi.order_id) as orders_count
FROM products p
JOIN order_items oi ON p.product_id = oi.product_id
JOIN orders o ON oi.order_id = o.order_id
WHERE o.order_status IN ('delivered', 'shipped')
GROUP BY p.product_id, p.product_name
ORDER BY total_sold DESC
LIMIT 10;

-- =============================================================================
-- 8. EDGE CASES AND ERROR CONDITION QUERIES
-- =============================================================================

-- Check for orphaned records
SELECT 'user_sessions_orphaned' as issue, COUNT(*) as count
FROM user_sessions s
LEFT JOIN users u ON s.user_id = u.user_id
WHERE u.user_id IS NULL
UNION ALL
SELECT 'reviews_orphaned' as issue, COUNT(*) as count
FROM reviews r
LEFT JOIN users u ON r.user_id = u.user_id
LEFT JOIN products p ON r.product_id = p.product_id
WHERE u.user_id IS NULL OR p.product_id IS NULL;

-- Validate data constraints
SELECT 'invalid_ratings' as issue, COUNT(*) as count
FROM reviews
WHERE rating < 1 OR rating > 5
UNION ALL
SELECT 'negative_prices' as issue, COUNT(*) as count
FROM products
WHERE price < 0
UNION ALL
SELECT 'negative_stock' as issue, COUNT(*) as count
FROM products
WHERE stock_quantity < 0;

-- Check for suspicious activity
SELECT 
    user_id,
    COUNT(*) as login_attempts,
    COUNT(DISTINCT DATE(login_time)) as unique_days,
    MIN(login_time) as first_attempt,
    MAX(login_time) as last_attempt
FROM user_sessions
WHERE login_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)
GROUP BY user_id
HAVING login_attempts > 10  -- Potential brute force attempts
ORDER BY login_attempts DESC;
