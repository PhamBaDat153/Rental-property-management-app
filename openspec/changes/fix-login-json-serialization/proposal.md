## Why

The login endpoint returns a JPA `User` entity directly. Jackson traverses its bidirectional relationships and can recurse through tenants, announcements, and maintenance requests until Spring fails with a document nesting depth error after the response has already started. Login also sends the password in a GET query string, where it can be captured by logs and intermediaries.

## What Changes

- Return a minimal login response DTO containing only the fields required by the Android client.
- Prevent persistence relationships from crossing the login API boundary.
- Change authentication transport to a POST request body so credentials are not placed in the URL.
- Update the Android Retrofit client and login call for the new request contract.
- Add regression coverage proving login serialization completes and does not expose entity graph or password data.
- Preserve existing authentication and authorization behavior, including unauthorized responses.

## Capabilities

### New Capabilities

- `login-api-safety`: Provide a bounded, credential-safe login request and response contract.

### Modified Capabilities

- `rental-resource-crud`: Successful resource responses, including authentication-related user responses, must use bounded DTOs and must not expose recursive persistence graphs or password data.

## Impact

- Backend login controller, request/response DTOs, and focused web/serialization tests.
- Android Retrofit API declaration and `LoginActivity` request handling.
- External login API changes from `GET /be/user/login` query parameters to `POST /be/user/login` JSON body.
- No database schema or authentication algorithm change.
