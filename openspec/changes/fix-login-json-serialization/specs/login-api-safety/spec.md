## Purpose

Provide a bounded and credential-safe login contract that completes reliably without exposing persistence relationships or sensitive authentication data.

## ADDED Requirements

### Requirement: Accept credentials through a request body
The system SHALL accept login credentials through a POST request body containing the username, password, and login type, and SHALL NOT require credentials in the request URL.

#### Scenario: Submit valid login credentials
- **WHEN** a client sends a POST login request with valid username, password, and supported login type
- **THEN** the system authenticates the user and returns a successful login response

#### Scenario: Reject invalid login credentials
- **WHEN** a client sends missing, malformed, or invalid credentials
- **THEN** the system returns an unauthorized or client-error response without exposing authentication details

### Requirement: Return a bounded login response
The system SHALL return only the authenticated user's identifier and username in a successful login response, and SHALL NOT serialize persistence relationships, password hashes, or unrelated user collections.

#### Scenario: Serialize a successful login
- **WHEN** authentication succeeds for a user with tenant or related records
- **THEN** the response is valid JSON containing the user identifier and username without recursive entity nesting

#### Scenario: Return unauthorized login
- **WHEN** authentication fails
- **THEN** the system returns an unauthorized response without a user entity or sensitive fields
