# Census App for QA testing

This is a full stack QA Testing Challenge. It has nothing to do with real Census project. Sole intention for this application is to facilitate QA testing for all 3 layers: UI, API, Database. When creating new demographic records please don't use any PII (Personal Identifiable Information) rather please create records of unreal people. Good practice for QA environment when creating dummy data is to utilize `FAKE` word in first and last names and emails.

## Interview instructions

You are expected to be on interview with

- locally running census app and postgres database
- Automation framework with test scenarios
- DBeaver connected to postgres database
- [Postman](https://www.postman.com/) with collection having API requests to census app.

## Content

- [1 Deploy census app](#1-deploy-census-app)
- [2 UI test preconditions](#2-ui-test-preconditions)
- [3 Framework setup](#3-framework-setup)
- [4 Create Automated tests](#4-create-automated-tests)
- [API documentation](#api-documentation)

## 1 Deploy census app

Please deploy application by following [local deployment instructions](my_support/local_deployment_instructions.md).

## 2 UI test preconditions

[Content](#content)

1. As you have application up and running please open url `localhost:3000/` on your preferred browser and manually register your first user and login.
2. Add household. Please don't use any real addresses.
3. Add record with relationship = `SELF`. Please don't use any PII (Personal Identifiable Information). When creating dummy data for personal demographic information QA best practice is to add `FAKE` word to beginning of names.

## 3 Framework setup

[Content](#content)

If your recruiter asks you to present automation framework on Java language, then please have your test automation framework setup using next software:

- IntelliJ IDE
- Java
- Maven
- Cucumber or JUnit or TestNG
- Selenium
- Rest assured
- JDBC
- Cucumber HTML Reporting (optional)
- Any screenshot taking library/dependency (optional)

Otherwise please review with your recruiter on language that should be used in this challenge.

## 4 Create Automated tests

[Content](#content)

Please create reusable parameterized Cucumber (BDD) steps in scenarios or using JUnit or TestNG (TDD) to test 3 user stories described below. Please try to utilize `configuration.properties` file of any of `.env` file approach to get some environment variable from.

Acceptance criteria:

1. **User should be able to add new person record to household on UI.**
   You can create your own Cucumber steps (BDD) or test scripts (TDD) to achieve goal. _As optional for extra points please save added personal demographic info to storage as POJO object or any other data format of your choice._

2. **Added person's record should be returned in API call.** Validate that added person's data returned in API call with correct values submitted on UI. Please refer to [API documentation](#api-documentation) below. _As optional for extra points please use [jackson-databind](https://github.com/FasterXML/jackson-databind) to create and compare POJO objects or compare saved data in your storage using data format of your choice._

3. **Added person's record should be found in database.** please review database schema on DBeaver to understand table relationships. Using your JDBC validate that new record saved with correct data. _As optional for extra points please use [jackson-databind](https://github.com/FasterXML/jackson-databind) to create and compare POJO objects or compare saved data in your storage using data format of your choice._

4. **As optional for extra points please** create automation scenarios updating, deleting records on UI

## API documentation

[Content](#content)

1. Login

**POST** `http://localhost/api/auth/login` with payload:

```json
{
  "email": "example@example.com",
  "password": "123456"
}
```

Token will be saved in cookies and will be automatically available for next API calls. So no need to save token. Token will be expired in 4 hours.

2. Get records for user by email

**GET** `http://localhost/api/record/user/email/[email]`. Please have your email instead of `[email]` endpoint parameter.

3. Get records for user by query parameters

**GET** `http://localhost/api/record/user`. At least one query params should be used. Query params in use order:

- email: string, email format
- id: number

## License

Licensed under the [MIT license](LICENSE.md).
