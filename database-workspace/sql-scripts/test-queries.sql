-- QA Test Queries based on study materials
-- These queries demonstrate common testing scenarios

-- 1. Users registered in last 7 days
SELECT user_id, username, email, first_name, last_name, registration_date
FROM Users
WHERE registration_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
ORDER BY registration_date DESC;

-- 2. Find duplicate records (by email)
SELECT email, COUNT(*) as duplicate_count
FROM Users
GROUP BY email
HAVING COUNT(*) > 1;

-- 3. Find second highest salary
SELECT MAX(salary) as second_highest_salary
FROM Employees
WHERE salary < (SELECT MAX(salary) FROM Employees);

-- Alternative approach for second highest salary
SELECT salary
FROM Employees
ORDER BY salary DESC
LIMIT 1 OFFSET 1;

-- 4. Customers with more than 5 orders
SELECT c.customer_id, c.name, c.email, COUNT(o.order_id) AS order_count
FROM Customers c
JOIN Orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name, c.email
HAVING COUNT(o.order_id) > 5
ORDER BY order_count DESC;

-- 5. Verify UI insert reflected in DB (example for user registration)
SELECT * FROM Users
WHERE username = 'qa_user'
AND email = 'qa@example.com'
AND registration_date >= DATE_SUB(NOW(), INTERVAL 1 HOUR);

-- 6. INNER JOIN example - Customers with their orders
SELECT c.name, c.email, o.order_id, o.total_amount, o.status, o.order_date
FROM Customers c
INNER JOIN Orders o ON c.customer_id = o.customer_id
ORDER BY c.name, o.order_date;

-- 7. LEFT JOIN example - All customers and their orders (including customers with no orders)
SELECT c.customer_id, c.name, c.email, o.order_id, o.total_amount, o.status
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id
ORDER BY c.name;

-- 8. RIGHT JOIN example - All orders and their customers
SELECT c.name, c.email, o.order_id, o.total_amount, o.status
FROM Customers c
RIGHT JOIN Orders o ON c.customer_id = o.customer_id
ORDER BY o.order_date;

-- 9. Users with no orders (customers who haven't placed any orders)
SELECT c.customer_id, c.name, c.email
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id
WHERE o.order_id IS NULL;

-- 10. Banking money transfer validation queries
-- Check sender balance before transfer
SELECT account_number, balance
FROM Accounts
WHERE account_id = 1;

-- Check receiver balance before transfer
SELECT account_number, balance
FROM Accounts
WHERE account_id = 2;

-- Verify transaction record exists
SELECT * FROM Transactions
WHERE from_account_id = 1 AND to_account_id = 2
AND amount = 500.00 AND status = 'completed';

-- Check balances after transfer (for validation)
SELECT 
    a1.account_number as sender_account,
    a1.balance as sender_balance,
    a2.account_number as receiver_account,
    a2.balance as receiver_balance,
    t.amount as transfer_amount,
    t.transaction_date
FROM Transactions t
JOIN Accounts a1 ON t.from_account_id = a1.account_id
JOIN Accounts a2 ON t.to_account_id = a2.account_id
WHERE t.transaction_id = 1;

-- 11. Performance testing queries
-- Find all orders for a specific customer (index on customer_id recommended)
SELECT * FROM Orders
WHERE customer_id = 1
ORDER BY order_date DESC;

-- Find users by email (unique index exists)
SELECT * FROM Users
WHERE email = 'qa@example.com';

-- 12. Data cleanup queries for test automation
-- Delete test user after test
DELETE FROM Users
WHERE username LIKE 'test_%' OR email LIKE 'test%@example.com';

-- Reset account balances for testing
UPDATE Accounts
SET balance = 5000.00
WHERE account_number IN ('ACC001', 'ACC002', 'ACC003');

-- 13. Edge case queries
-- Users with null last_login (never logged in)
SELECT username, email, registration_date, last_login
FROM Users
WHERE last_login IS NULL;

-- Orders with zero or negative amounts (data validation)
SELECT * FROM Orders
WHERE total_amount <= 0;

-- 14. Aggregation queries for reporting tests
-- Monthly order summary
SELECT 
    YEAR(order_date) as year,
    MONTH(order_date) as month,
    COUNT(*) as total_orders,
    SUM(total_amount) as total_revenue,
    AVG(total_amount) as average_order_value
FROM Orders
WHERE status = 'delivered'
GROUP BY YEAR(order_date), MONTH(order_date)
ORDER BY year DESC, month DESC;

-- Customer order statistics
SELECT 
    c.name,
    COUNT(o.order_id) as total_orders,
    SUM(o.total_amount) as total_spent,
    AVG(o.total_amount) as average_order,
    MAX(o.total_amount) as highest_order,
    MIN(o.total_amount) as lowest_order
FROM Customers c
LEFT JOIN Orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name
ORDER BY total_spent DESC;
