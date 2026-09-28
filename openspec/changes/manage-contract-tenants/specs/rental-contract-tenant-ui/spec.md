## Purpose

Provide landlords with an integrated contract-detail workflow for managing tenant assignments, assignment dates, and representative selection without exposing composite persistence details.

## ADDED Requirements

### Requirement: Display assigned tenants in contract detail
The Android application SHALL display the tenant assignments belonging to the current contract inside its detail screen. Each row SHALL resolve the tenant identifier to readable tenant context and show assignment dates, representative state, and read-only assignment status.

#### Scenario: Display assigned tenants
- **WHEN** the contract detail and assignment data load successfully
- **THEN** the detail screen shows each assignment belonging to the contract with tenant name or `Chưa cập nhật`, move-in date, move-out date, representative label, and status label

#### Scenario: No tenants assigned
- **WHEN** the contract has no tenant assignments
- **THEN** the detail screen shows an explicit empty state and an action to add a tenant

#### Scenario: Assignment tenant profile is unavailable
- **WHEN** an assignment references a tenant that is not present in the loaded tenant list
- **THEN** the assignment remains visible with `Chưa cập nhật` tenant information and does not expose technical identifiers as the primary label

### Requirement: Create and edit contract tenant assignments
The Android application SHALL allow adding an existing tenant to the current contract and editing assignment move-in date, move-out date, and representative selection. Contract and tenant identifiers SHALL remain immutable after creation, and assignment status SHALL be displayed but not edited.

#### Scenario: Add a tenant to a contract
- **WHEN** the user selects an available tenant, chooses representative state, enters valid optional dates, and saves
- **THEN** the application creates the assignment for the current contract and refreshes the assignment list

#### Scenario: Tenant is already assigned to the contract
- **WHEN** the user attempts to add a tenant already assigned to the current contract
- **THEN** the application reports a duplicate-assignment conflict and preserves the existing list

#### Scenario: Reject invalid assignment dates
- **WHEN** the move-out date is before the move-in date
- **THEN** the form remains open, shows an actionable Vietnamese validation message, and does not submit

#### Scenario: Edit assignment values
- **WHEN** the user edits valid dates or representative state for an existing assignment
- **THEN** the application updates only that assignment and displays the returned values

### Requirement: Manage one representative per contract
The system SHALL allow zero or one representative tenant for each contract. When an assignment is created or updated as representative, the previous representative assignment for the same contract SHALL be unset atomically. Representative state in assignments for other contracts SHALL not change.

#### Scenario: Select a new representative
- **WHEN** the user marks a tenant as representative for a contract that already has a representative
- **THEN** the previous representative is automatically changed to non-representative and the selected tenant becomes the only representative

#### Scenario: Remove the current representative
- **WHEN** the user edits the current representative assignment to non-representative or deletes it
- **THEN** the contract remains valid with no representative assigned

#### Scenario: Reuse a tenant across contracts
- **WHEN** a tenant assigned to one contract is added to a different contract
- **THEN** the second assignment is allowed and representative state is evaluated independently per contract

### Requirement: Delete assignments safely
The Android application SHALL provide a confirmed delete action for an assignment and SHALL keep the user on the contract detail screen when deletion fails.

#### Scenario: Delete an assignment
- **WHEN** the user confirms deletion of a tenant assignment
- **THEN** the application deletes only that assignment and refreshes the current contract's assignment list

#### Scenario: Cancel assignment deletion
- **WHEN** the user cancels the delete confirmation
- **THEN** the assignment remains unchanged
