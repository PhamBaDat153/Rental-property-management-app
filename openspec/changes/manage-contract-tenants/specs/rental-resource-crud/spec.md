## MODIFIED Requirements

### Requirement: Manage contract-tenant assignments
The system SHALL allow clients to add, retrieve, update, and remove tenant assignments for existing contracts and tenants. Assignment requests MUST require existing contract and tenant identifiers, validate move-in and move-out dates, and represent representative status explicitly. A tenant MAY be assigned to multiple different contracts, but a tenant MUST NOT be assigned more than once to the same contract. Each contract MAY have zero or one representative assignment; creating or updating a representative assignment MUST unset any existing representative assignment for that contract atomically.

#### Scenario: Add a valid contract tenant
- **WHEN** a client submits an existing contract identifier, existing tenant identifier, valid representative flag, and valid optional dates
- **THEN** the system creates the assignment and returns its composite identifiers and assignment values

#### Scenario: Reject an invalid assignment
- **WHEN** a client submits a null identifier, missing referenced contract or tenant, or a move-out date before move-in
- **THEN** the system returns a validation or not-found error and does not persist the assignment

#### Scenario: Prevent duplicate assignment
- **WHEN** a client adds a tenant already assigned to the same contract
- **THEN** the system returns a conflict response without creating a duplicate assignment

#### Scenario: Allow tenant reuse across contracts
- **WHEN** a client adds a tenant assigned to one contract to a different contract
- **THEN** the system creates the second assignment and evaluates representative state independently for the second contract

#### Scenario: Replace a contract representative
- **WHEN** a client creates or updates an assignment as representative for a contract with an existing representative
- **THEN** the system unsets the previous representative and persists the requested assignment as the only representative in one transaction

#### Scenario: Allow no representative
- **WHEN** a client unsets or deletes the current representative assignment
- **THEN** the system accepts the contract with no representative assignment

#### Scenario: Remove an assignment
- **WHEN** a client removes an existing contract-tenant assignment
- **THEN** the system deletes only that assignment and returns a successful no-content response
