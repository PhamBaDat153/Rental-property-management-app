## Purpose

Provide landlords with an in-context workflow to manage the tenants assigned to a rental contract without leaving the contract detail screen.

## ADDED Requirements

### Requirement: Display tenants assigned to a contract
The Android application SHALL show a ContractTenant section in the detail screen for the selected contract. It SHALL resolve assignment tenant identifiers against tenant profile data and display each tenant's readable name, representative state, dates, and status text.

#### Scenario: Display assigned tenants
- **WHEN** the contract detail screen loads assignments and tenant profiles successfully
- **THEN** it shows only assignments for the current contract with readable tenant context and no raw identifier as the primary label

#### Scenario: No tenants assigned
- **WHEN** the assignment request succeeds with no records for the current contract
- **THEN** the detail screen shows a Vietnamese empty state and offers an add-tenant action

#### Scenario: Tenant profile cannot be resolved
- **WHEN** an assignment references a tenant absent from the loaded tenant profile list
- **THEN** the assignment remains visible with `Chưa cập nhật người thuê` and remains editable or removable by its composite identifiers

### Requirement: Add and edit contract tenant assignments
The Android application SHALL allow adding an existing tenant to the current contract and editing representative selection, optional move-in date, and optional move-out date. Contract and tenant identifiers SHALL remain immutable after creation, and assignment status SHALL be displayed read-only.

#### Scenario: Add a valid assignment
- **WHEN** the user selects an available tenant and submits valid dates and representative choice
- **THEN** the application creates the assignment for the current contract and refreshes the assignment section

#### Scenario: Edit assignment details
- **WHEN** the user changes representative choice or valid move-in/move-out dates
- **THEN** the application updates the existing composite-key assignment and displays the returned values

#### Scenario: Reject invalid assignment dates
- **WHEN** move-out date is before move-in date
- **THEN** the application keeps the form open, shows an actionable Vietnamese validation message, and does not submit

#### Scenario: Prevent duplicate tenant assignment
- **WHEN** the user attempts to add a tenant already assigned to the current contract
- **THEN** the application prevents the duplicate locally or reports the backend conflict without removing the existing assignment

### Requirement: Maintain one representative per contract
The system SHALL allow zero or one representative assignment for each contract. When an assignment is created or updated as representative, the previous representative for that same contract SHALL be cleared in the same transaction. Assignments for the same tenant on other contracts SHALL remain unchanged.

#### Scenario: Select a new representative
- **WHEN** a tenant assignment is created or updated with representative status true
- **THEN** the previous representative for that contract becomes non-representative and the selected assignment becomes representative atomically

#### Scenario: Remove the only representative
- **WHEN** the current representative is updated as non-representative or deleted
- **THEN** the contract remains valid with no representative assignment

#### Scenario: Tenant appears in another contract
- **WHEN** a tenant assigned to one contract is assigned to a different contract
- **THEN** the second assignment is allowed and representative state changes are isolated per contract

### Requirement: Delete contract tenant assignments safely
The Android application SHALL require explicit confirmation before deleting an assignment and SHALL refresh the current contract detail only after a successful deletion.

#### Scenario: Confirm assignment deletion
- **WHEN** the user chooses to remove a tenant from a contract
- **THEN** the application explains the consequence, requires confirmation, and deletes only the selected composite-key assignment

#### Scenario: Assignment deletion fails
- **WHEN** the backend rejects or cannot process the deletion
- **THEN** the application keeps the assignment visible and shows a recoverable Vietnamese error
