## MODIFIED Requirements

### Requirement: Return stable validation and relationship errors
The system SHALL return structured, client-readable error responses for malformed input, missing resources, duplicate values, relationship conflicts, and persistence failures. Successful resource responses MUST use bounded DTOs and MUST NOT expose password hashes, recursive entity graphs, or internal persistence collections, including for authentication responses.

#### Scenario: Report malformed JSON or null input
- **WHEN** a client sends malformed JSON or omits a required request field
- **THEN** the system returns a client-error response identifying the invalid request without writing data

#### Scenario: Report relationship conflict
- **WHEN** a requested delete or update violates a current foreign-key relationship or domain guard
- **THEN** the system returns a conflict response with a stable error shape and keeps the affected data unchanged

#### Scenario: Bound successful authentication output
- **WHEN** a client successfully authenticates
- **THEN** the system returns a bounded authentication DTO and does not serialize password hashes, persistence relationships, or internal collections
