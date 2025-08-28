# Database Testing Workspace

This project provides a workspace for testing database interactions using Java, Maven, and TestNG. It includes a suite of tests that demonstrate various database testing scenarios, from simple connection tests to complex business logic validation.

## Project Structure

- `src/main/java`: Contains the main application code, including database connection and query execution utilities.
- `src/test/java`: Contains the TestNG test suite.
- `pom.xml`: The Maven project configuration file, managing dependencies and build settings.
- `sql-scripts`: Contains SQL scripts for schema creation and test queries.

## QueryExecutor Methods

Here is a detailed description of each of the public methods in the `QueryExecutor` class:

- `executeQuery(String query, Object... parameters)`: Executes a `SELECT` query with the given parameters and returns the results as a `List` of `Map` objects, where each map represents a row with column names as keys.

- `executeUpdate(String query, Object... parameters)`: Executes an `INSERT`, `UPDATE`, or `DELETE` query with the given parameters and returns the number of affected rows.

- `executeScalar(String query, Object... parameters)`: Executes a query and returns a single value from the first row and first column of the result set.

- `recordExists(String tableName, String whereClause, Object... parameters)`: Checks if a record exists in a specified table based on a `WHERE` clause.

- `getRecordCount(String tableName, String whereClause, Object... parameters)`: Returns the count of records in a table that match the given `WHERE` clause.

- `verifyUserInsert(String username, String email)`: Verifies that a user with the specified username and email has been inserted into the `users` table within the last hour.

- `getUsersRegisteredInLastDays(int days)`: Retrieves a list of users who have registered within the last N days.

- `findDuplicateEmails()`: Finds email addresses that are used by more than one user in the `users` table.

- `getSecondHighestSalary()`: Retrieves the second highest salary from the `employees` table.

- `getCustomersWithMoreThanNOrders(int orderCount)`: Retrieves a list of customers who have placed more than a specified number of orders.

- `getCustomersWithNoOrders()`: Retrieves a list of customers who have not placed any orders.

- `verifyTransfer(int fromAccountId, int toAccountId, double amount)`: Verifies if a banking transfer transaction between two accounts was completed successfully.

- `getAccountBalance(int accountId)`: Retrieves the account balance for a given account ID.

- `createDatabaseIfNotExists()`: Creates the `qa_test_db` database if it does not already exist.

- `createTables()`: A convenience method that calls all the individual table creation methods to set up the full database schema.

- `createUsersTable()`, `createEmployeesTable()`, `createCustomersTable()`, `createOrdersTable()`, `createAccountsTable()`, `createTransactionsTable()`: These methods create the individual tables required for the tests, dropping them first if they already exist.

- `setupTestData()`: Populates the database with a predefined set of test data for users, employees, customers, orders, and accounts.

- `cleanupTestData()`: Removes all test data from the database tables.

## Test Suite

The `DatabaseTest` class contains a suite of TestNG tests that cover various aspects of database testing.

### Setup and Teardown

- `setupClass()`: This method runs once before any tests in the class. It prepares the test environment by creating the database and tables, and then populating them with test data.

- `teardownClass()`: This method runs once after all tests in the class have completed. It cleans up the test data and closes the database connection pool.

### Tests

- `testDatabaseConnection()`: A simple smoke test to verify that a connection to the database can be established and that basic queries can be executed.

- `testUsersRegisteredInLastSevenDays()`: A regression test that verifies the retrieval of users who have registered in the last 7 days.

- `testFindDuplicateEmails()`: A data validation test that checks the functionality for finding duplicate email addresses in the `users` table.

- `testSecondHighestSalary()`: A regression test that verifies the retrieval of the second highest salary from the `employees` table.

- `testCustomersWithMoreThanFiveOrders()`: A business logic test that verifies the retrieval of customers who have placed more than 5 orders.

- `testCustomersWithNoOrders()`: A business logic test that verifies the retrieval of customers who have not placed any orders.

- `testVerifyUIInsertReflectedInDB()`: A UI validation test that simulates a UI action of inserting a new user and verifies that the user is correctly saved in the database.

- `testBankingTransferValidation()`: A test that simulates a banking transfer and verifies that the transaction is recorded correctly and that the account balances are updated as expected.

- `testQueryPerformance()`: A performance test that checks a potentially slow query to ensure it completes within a reasonable time frame (under 5 seconds).

- `testDataIntegrityConstraints()`: A data integrity test that attempts to insert a user with a duplicate username and email to ensure the database's unique constraints are working correctly.

- `testEdgeCases()`: This test covers various edge cases, such as handling of `NULL` values in the database and ensuring that queries for non-existent data return empty result sets as expected.
