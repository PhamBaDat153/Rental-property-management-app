## MODIFIED Requirements

### Requirement: Manage contract-tenant assignments
The system SHALL allow clients to add, retrieve, update, and remove tenant assignments for existing contracts and tenants. Assignment requests MUST require existing contract and tenant identifiers, validate move-in and move-out dates, and represent representative status explicitly. A tenant MAY be assigned to multiple different contracts, but a tenant MUST NOT be assigned more than once to the same contract. Each contract MAY have zero or one representative assignment; selecting a representative MUST clear any previous representative for that contract atomically.

#### Scenario: Add a valid contract tenant
- **WHEN** a client submits an existing contract identifier, existing tenant identifier, valid representative flag, and valid optional dates
- **THEN** the system creates the assignment and returns its composite identifiers and assignment values

#### Scenario: Allow tenant participation in multiple contracts
- **WHEN** a tenant is assigned to a second contract with a different contract identifier
- **THEN** the system allows the assignment and does not alter the tenant's assignment on the first contract

#### Scenario: Reject duplicate assignment in one contract
- **WHEN** a client adds a tenant already assigned to the same contract
- **THEN** the system returns a conflict response without creating a duplicate assignment

#### Scenario: Prevent duplicate assignment
- **WHEN** a client attempts to add a tenant already assigned to the same contract
- **THEN** the system rejects the request with a conflict response and leaves the existing assignment unchanged

#### Scenario: Reject an invalid assignment
- **WHEN** a client submits a null identifier, missing referenced contract or tenant, or a move-out date before move-in
- **THEN** the system returns a validation or not-found error and does not persist the assignment

#### Scenario: Replace the representative
- **WHEN** a client creates or updates an assignment as representative for a contract that already has a representative
- **THEN** the system clears the previous representative, sets the selected assignment as representative, and commits both changes atomically

#### Scenario: Remove the only representative
- **WHEN** a client updates the only representative to false or deletes that assignment
- **THEN** the system succeeds and leaves the contract with no representative assignment

#### Scenario: Remove an assignment
- **WHEN** a client removes an existing contract-tenant assignment
- **THEN** the system deletes only that assignment and returns a successful no-content response
