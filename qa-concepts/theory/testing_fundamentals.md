# QA Testing Fundamentals

## Software Testing Lifecycle (STLC)

### 1. Requirement Analysis
- **Objective**: Understand and analyze requirements from a testing perspective
- **Activities**:
  - Review functional and non-functional requirements
  - Identify testable requirements
  - Determine test environment requirements
  - Perform requirement traceability analysis
- **Deliverables**:
  - Requirement Traceability Matrix (RTM)
  - Test environment setup requirements
  - Automation feasibility analysis

### 2. Test Planning
- **Objective**: Define the scope, approach, resources, and schedule of testing activities
- **Activities**:
  - Develop test strategy and test plan
  - Identify test data requirements
  - Estimate effort and resources
  - Define entry and exit criteria
- **Deliverables**:
  - Test Plan document
  - Test Strategy document
  - Effort estimation document

### 3. Test Case Development
- **Objective**: Create detailed test cases and test scripts
- **Activities**:
  - Write test cases based on requirements
  - Review and baseline test cases
  - Create test data
  - Prepare test environment setup
- **Deliverables**:
  - Test cases and test scripts
  - Test data
  - Test environment setup guide

### 4. Test Environment Setup
- **Objective**: Set up the test environment for test execution
- **Activities**:
  - Environment setup and configuration
  - Test data preparation
  - Smoke testing of environment
- **Deliverables**:
  - Test environment ready for execution
  - Test data setup
  - Environment setup documentation

### 5. Test Execution
- **Objective**: Execute test cases and report defects
- **Activities**:
  - Execute test cases
  - Report defects
  - Map defects to test cases
  - Retest defect fixes
- **Deliverables**:
  - Test execution reports
  - Defect reports
  - Updated test cases

### 6. Test Closure
- **Objective**: Wrap up testing activities and document lessons learned
- **Activities**:
  - Evaluate test completion criteria
  - Document lessons learned
  - Prepare test summary report
  - Archive test artifacts
- **Deliverables**:
  - Test closure report
  - Test metrics and analysis
  - Lessons learned document

## Defect Life Cycle

### Defect States
1. **New** - Defect is identified and logged
2. **Assigned** - Defect is assigned to developer
3. **Open** - Developer starts working on the defect
4. **Fixed** - Developer fixes the defect
5. **Retest** - Defect is ready for retesting
6. **Verified** - Defect fix is verified by tester
7. **Closed** - Defect is closed after verification
8. **Reopened** - Defect is reopened if fix is not working
9. **Rejected** - Defect is rejected if not valid
10. **Deferred** - Defect is deferred to future release

### Defect Severity vs Priority

#### Severity (Impact on System)
- **Critical**: System crash, data loss, security breach
- **High**: Major functionality not working
- **Medium**: Minor functionality issues
- **Low**: Cosmetic issues, suggestions

#### Priority (Business Importance)
- **High**: Must fix before release
- **Medium**: Should fix if time permits
- **Low**: Can be fixed in future releases

## Types of Testing

### 1. Functional Testing
- **Unit Testing**: Testing individual components
- **Integration Testing**: Testing component interactions
- **System Testing**: Testing complete system
- **Acceptance Testing**: Testing from user perspective

### 2. Non-Functional Testing
- **Performance Testing**: Response time, throughput
- **Load Testing**: Normal expected load
- **Stress Testing**: Beyond normal capacity
- **Volume Testing**: Large amounts of data
- **Security Testing**: Vulnerabilities and threats
- **Usability Testing**: User experience
- **Compatibility Testing**: Different environments

### 3. Based on Test Design Technique
- **Black Box Testing**: Testing without knowledge of internal structure
- **White Box Testing**: Testing with knowledge of internal structure
- **Gray Box Testing**: Combination of black box and white box

### 4. Based on Test Execution
- **Manual Testing**: Tests executed manually by testers
- **Automated Testing**: Tests executed by automation tools

### 5. Based on Testing Approach
- **Static Testing**: Testing without executing code (reviews, walkthroughs)
- **Dynamic Testing**: Testing by executing code

## Test Design Techniques

### 1. Black Box Techniques

#### Equivalence Partitioning
- Divide input domain into equivalence classes
- Test one value from each class
- Reduces number of test cases

**Example**: Age field (1-100)
- Valid partition: 1-100
- Invalid partitions: <1, >100

#### Boundary Value Analysis
- Test values at boundaries of equivalence classes
- Test minimum, maximum, and just outside boundaries

**Example**: Age field (18-65)
- Test values: 17, 18, 19, 64, 65, 66

#### Decision Table Testing
- Systematic approach for complex business rules
- All combinations of conditions and actions

#### State Transition Testing
- Test different states and transitions
- Useful for systems with different states

### 2. White Box Techniques

#### Statement Coverage
- Every statement in code is executed at least once
- Coverage = (Statements executed / Total statements) × 100

#### Branch Coverage
- Every branch (decision point) is executed
- Both true and false outcomes tested

#### Path Coverage
- Every possible path through code is executed
- Most thorough but often impractical

## Agile Testing

### Agile Testing Principles
1. Testing is not a phase but an activity
2. Prevent bugs rather than finding bugs
3. Don't be a gatekeeper, be a facilitator
4. Focus on business value
5. Adapt to change

### Testing in Scrum
- **Sprint Planning**: Test planning and estimation
- **Daily Standups**: Testing progress and blockers
- **Sprint Review**: Demo tested features
- **Sprint Retrospective**: Improve testing process

### Test Pyramid
1. **Unit Tests** (Base): Fast, isolated, many
2. **Integration Tests** (Middle): Medium speed, moderate number
3. **UI Tests** (Top): Slow, expensive, few

### Definition of Done (DoD)
- Code is written and reviewed
- Unit tests are written and passing
- Integration tests are passing
- Feature is tested by QA
- Documentation is updated
- Acceptance criteria are met

## Risk-Based Testing

### Risk Assessment
1. **Identify Risks**: Technical, business, project risks
2. **Analyze Risks**: Probability and impact
3. **Prioritize Risks**: Risk matrix (High, Medium, Low)
4. **Mitigate Risks**: Testing strategies to address risks

### Risk Matrix
```
           High Impact    Medium Impact    Low Impact
High Prob     Critical      High           Medium
Med Prob      High          Medium         Low
Low Prob      Medium        Low            Low
```

## Test Metrics and Reporting

### Key Metrics
1. **Test Coverage**: Requirements, code, functionality covered
2. **Defect Density**: Defects per unit of code/functionality
3. **Defect Removal Efficiency**: Defects found in testing vs production
4. **Test Execution Progress**: Planned vs executed vs passed
5. **Defect Age**: Time from defect creation to closure

### Test Reports
- **Daily Test Status Report**: Progress, issues, risks
- **Test Summary Report**: Overall test results and metrics
- **Defect Report**: Defect status and trends
- **Test Closure Report**: Final test summary and recommendations

## Quality Assurance vs Quality Control

### Quality Assurance (QA)
- **Focus**: Process-oriented
- **Approach**: Preventive
- **Responsibility**: Everyone in team
- **Activities**: Process definition, training, audits
- **Goal**: Prevent defects

### Quality Control (QC)
- **Focus**: Product-oriented
- **Approach**: Detective
- **Responsibility**: Testing team
- **Activities**: Testing, reviews, inspections
- **Goal**: Find and fix defects

## Test Automation Strategy

### Automation Pyramid
1. **Unit Tests**: 70% - Fast, reliable, cheap
2. **Integration Tests**: 20% - Medium speed and cost
3. **UI Tests**: 10% - Slow, expensive, brittle

### When to Automate
- **Good Candidates**:
  - Repetitive tests
  - Regression tests
  - Data-driven tests
  - Performance tests
  - Smoke tests

- **Poor Candidates**:
  - One-time tests
  - Exploratory tests
  - Usability tests
  - Tests requiring human judgment

### ROI of Automation
```
ROI = (Savings from automation - Cost of automation) / Cost of automation × 100

Savings = (Manual execution time × Number of executions × Hourly rate)
Cost = Development time + Maintenance time + Tool cost
```

## Best Practices

### Test Case Design
1. Write clear, concise test cases
2. Include expected results
3. Make tests independent
4. Use descriptive names
5. Maintain traceability to requirements

### Test Data Management
1. Use realistic test data
2. Protect sensitive data
3. Maintain data consistency
4. Automate data setup/cleanup
5. Version control test data

### Test Environment Management
1. Keep environments consistent
2. Automate environment setup
3. Monitor environment health
4. Document environment configurations
5. Implement proper access controls

### Defect Management
1. Write clear defect descriptions
2. Include steps to reproduce
3. Attach relevant screenshots/logs
4. Classify severity and priority correctly
5. Track defect metrics and trends
