# Login Page Test Cases
**Exercise: Write test cases for a login page (web client)**

## Test Case 1: Valid Login

### Test Case Information
- **Test Case ID**: TC_LOGIN_001
- **Test Case Title**: Verify successful login with valid credentials
- **Module/Feature**: User Authentication
- **Priority**: High
- **Test Type**: Functional
- **Test Level**: System

### Prerequisites
- User account exists in the system
- Login page is accessible
- Valid username and password are available

### Test Objective
Verify that a user can successfully log in with valid credentials and is redirected to the appropriate dashboard/home page.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | qa_user | Valid registered username |
| Password | Password123! | Valid password for the user |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page is displayed with username and password fields |
| 2 | Enter valid username in the username field | Username is entered and displayed in the field |
| 3 | Enter valid password in the password field | Password is entered and masked with asterisks |
| 4 | Click the "Login" button | User is successfully authenticated and redirected to dashboard |
| 5 | Verify user is logged in | User's name/profile is displayed, logout option is available |

### Expected Results
- User is successfully logged in
- Redirected to the main dashboard/home page
- User session is established
- No error messages are displayed

---

## Test Case 2: Invalid Username

### Test Case Information
- **Test Case ID**: TC_LOGIN_002
- **Test Case Title**: Verify login fails with invalid username
- **Module/Feature**: User Authentication
- **Priority**: High
- **Test Type**: Functional - Negative Testing
- **Test Level**: System

### Prerequisites
- Login page is accessible
- Invalid username that doesn't exist in the system

### Test Objective
Verify that login fails when an invalid/non-existent username is provided.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | invalid_user_123 | Non-existent username |
| Password | Password123! | Valid password format |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page is displayed |
| 2 | Enter invalid username | Username is entered in the field |
| 3 | Enter any password | Password is entered and masked |
| 4 | Click the "Login" button | Login fails with appropriate error message |
| 5 | Verify error message | Error message indicates invalid credentials |

### Expected Results
- Login fails
- Appropriate error message is displayed (e.g., "Invalid username or password")
- User remains on the login page
- No sensitive information is revealed about which field is incorrect

---

## Test Case 3: Invalid Password

### Test Case Information
- **Test Case ID**: TC_LOGIN_003
- **Test Case Title**: Verify login fails with invalid password
- **Module/Feature**: User Authentication
- **Priority**: High
- **Test Type**: Functional - Negative Testing
- **Test Level**: System

### Prerequisites
- Login page is accessible
- Valid username exists in the system
- Invalid password

### Test Objective
Verify that login fails when a valid username is provided with an incorrect password.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | qa_user | Valid registered username |
| Password | WrongPassword123 | Incorrect password |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page is displayed |
| 2 | Enter valid username | Username is entered correctly |
| 3 | Enter incorrect password | Password is entered and masked |
| 4 | Click the "Login" button | Login fails with error message |
| 5 | Verify error handling | Appropriate error message is shown |

### Expected Results
- Login fails
- Generic error message is displayed
- User remains on login page
- Account is not locked after single failed attempt

---

## Test Case 4: Empty Fields Validation

### Test Case Information
- **Test Case ID**: TC_LOGIN_004
- **Test Case Title**: Verify validation for empty username and password fields
- **Module/Feature**: User Authentication - Field Validation
- **Priority**: Medium
- **Test Type**: Functional - Boundary Testing
- **Test Level**: System

### Prerequisites
- Login page is accessible

### Test Objective
Verify that appropriate validation messages are displayed when username and/or password fields are left empty.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | [empty] | No input provided |
| Password | [empty] | No input provided |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page is displayed |
| 2 | Leave username field empty | Field remains empty |
| 3 | Leave password field empty | Field remains empty |
| 4 | Click the "Login" button | Validation messages appear |
| 5 | Verify validation messages | Appropriate messages for required fields |

### Expected Results
- Login button click triggers client-side validation
- Error messages indicate required fields
- Form submission is prevented
- Focus is set to the first empty required field

---

## Test Case 5: SQL Injection Attempt

### Test Case Information
- **Test Case ID**: TC_LOGIN_005
- **Test Case Title**: Verify system security against SQL injection attacks
- **Module/Feature**: User Authentication - Security
- **Priority**: High
- **Test Type**: Security Testing
- **Test Level**: System

### Prerequisites
- Login page is accessible
- Understanding of SQL injection techniques

### Test Objective
Verify that the login system is protected against SQL injection attacks and handles malicious input appropriately.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | admin' OR '1'='1' -- | SQL injection attempt |
| Password | password | Any password |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page is displayed |
| 2 | Enter SQL injection string in username | Malicious input is entered |
| 3 | Enter any password | Password is entered |
| 4 | Click the "Login" button | Login fails securely |
| 5 | Verify security handling | No unauthorized access granted |

### Expected Results
- Login fails
- No SQL injection is successful
- Generic error message is displayed
- System logs the security attempt
- No database errors are exposed to the user

---

## Test Case 6: Account Lockout Policy

### Test Case Information
- **Test Case ID**: TC_LOGIN_006
- **Test Case Title**: Verify account lockout after multiple failed login attempts
- **Module/Feature**: User Authentication - Security Policy
- **Priority**: High
- **Test Type**: Security Testing
- **Test Level**: System

### Prerequisites
- Valid user account exists
- Account lockout policy is configured (e.g., 5 failed attempts)
- Login page is accessible

### Test Objective
Verify that user accounts are locked after a specified number of consecutive failed login attempts.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | qa_user | Valid username |
| Password | WrongPassword | Incorrect password |
| Max Attempts | 5 | System-configured lockout threshold |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page is displayed |
| 2 | Enter valid username and wrong password | Credentials entered |
| 3 | Click Login button | Login fails with error message |
| 4 | Repeat steps 2-3 for configured max attempts | Each attempt fails |
| 5 | Attempt login again after max attempts reached | Account lockout message displayed |
| 6 | Try login with correct credentials | Login still fails due to lockout |

### Expected Results
- Account is locked after maximum failed attempts
- Clear lockout message is displayed
- Even correct credentials cannot unlock the account
- Lockout is logged in system audit trail

---

## Test Case 7: Password Visibility Toggle

### Test Case Information
- **Test Case ID**: TC_LOGIN_007
- **Test Case Title**: Verify password visibility toggle functionality
- **Module/Feature**: User Authentication - UI/UX
- **Priority**: Low
- **Test Type**: Functional - UI Testing
- **Test Level**: System

### Prerequisites
- Login page has password visibility toggle feature
- Login page is accessible

### Test Objective
Verify that users can toggle password visibility to see/hide their entered password.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Password | TestPassword123! | Sample password with special characters |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to the login page | Login page with password field is displayed |
| 2 | Enter password in password field | Password is masked with asterisks/dots |
| 3 | Click the "Show Password" icon/button | Password becomes visible as plain text |
| 4 | Click the "Hide Password" icon/button | Password is masked again |
| 5 | Verify toggle works multiple times | Toggle functionality works consistently |

### Expected Results
- Password visibility can be toggled on/off
- Icon/button changes to reflect current state
- Password field content remains unchanged during toggle
- Toggle works for any password length and character types

---

## Test Case 8: Browser Back Button Behavior

### Test Case Information
- **Test Case ID**: TC_LOGIN_008
- **Test Case Title**: Verify behavior when using browser back button after login
- **Module/Feature**: User Authentication - Session Management
- **Priority**: Medium
- **Test Type**: Functional - Navigation Testing
- **Test Level**: System

### Prerequisites
- Valid user credentials
- User is logged into the system
- Browser supports back button functionality

### Test Objective
Verify that using the browser back button after successful login doesn't compromise security or cause unexpected behavior.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | qa_user | Valid username |
| Password | Password123! | Valid password |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to login page | Login page is displayed |
| 2 | Enter valid credentials and login | Successfully logged in and redirected |
| 3 | Click browser back button | Appropriate behavior is observed |
| 4 | Verify current page state | User session and security are maintained |
| 5 | Test navigation from current state | Normal application flow continues |

### Expected Results
- Back button either shows dashboard or prevents going back to login
- User session remains active and secure
- No cached login page is displayed
- Application maintains proper state

---

## Test Case 9: Remember Me Functionality

### Test Case Information
- **Test Case ID**: TC_LOGIN_009
- **Test Case Title**: Verify "Remember Me" checkbox functionality
- **Module/Feature**: User Authentication - Session Persistence
- **Priority**: Medium
- **Test Type**: Functional
- **Test Level**: System

### Prerequisites
- Login page has "Remember Me" checkbox
- Valid user credentials
- Browser supports cookies

### Test Objective
Verify that the "Remember Me" feature correctly saves user credentials and maintains login state across browser sessions.

### Test Data
| Field Name | Value | Description |
|------------|-------|-------------|
| Username | qa_user | Valid username |
| Password | Password123! | Valid password |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Navigate to login page | Login page with "Remember Me" checkbox |
| 2 | Enter valid credentials | Credentials are entered |
| 3 | Check the "Remember Me" checkbox | Checkbox is selected |
| 4 | Click Login button | Successful login occurs |
| 5 | Close browser completely | Browser is closed |
| 6 | Reopen browser and navigate to site | User should still be logged in OR credentials pre-filled |

### Expected Results
- "Remember Me" functionality works as designed
- User credentials are securely stored (if applicable)
- Persistent login session is maintained (if applicable)
- Security best practices are followed

---

## Test Case 10: Responsive Design Validation

### Test Case Information
- **Test Case ID**: TC_LOGIN_010
- **Test Case Title**: Verify login page responsive design across different devices
- **Module/Feature**: User Authentication - UI/UX Responsive Design
- **Priority**: Medium
- **Test Type**: UI/UX Testing
- **Test Level**: System

### Prerequisites
- Login page should be responsive
- Access to different device viewports or browser dev tools

### Test Objective
Verify that the login page displays correctly and functions properly across different screen sizes and devices.

### Test Data
| Device Type | Screen Resolution | Description |
|-------------|------------------|-------------|
| Desktop | 1920x1080 | Large desktop screen |
| Tablet | 768x1024 | Standard tablet size |
| Mobile | 375x667 | Standard mobile phone |

### Test Steps
| Step # | Action | Expected Result |
|--------|--------|-----------------|
| 1 | Open login page on desktop | Page displays correctly with proper layout |
| 2 | Resize browser to tablet dimensions | Page adapts to tablet layout |
| 3 | Resize browser to mobile dimensions | Page adapts to mobile layout |
| 4 | Test login functionality on each size | Login works on all screen sizes |
| 5 | Verify touch targets on mobile | Buttons and fields are easily tappable |

### Expected Results
- Login page is fully responsive
- All elements are properly sized and positioned
- Text is readable on all screen sizes
- Interactive elements are accessible
- Login functionality works across all viewports
