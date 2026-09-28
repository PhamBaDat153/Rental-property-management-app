## Purpose

Provide landlords with a room-scoped workflow for configuring utility meters, recording meter readings, and assigning existing service-catalog entries without leaving the room management experience.

## ADDED Requirements

### Requirement: Manage room meters
The system SHALL allow a landlord to list, create, edit, and delete meters for a selected room, with each meter displaying its type and availability status.

#### Scenario: Display meters for a room
- **WHEN** the landlord opens utility management for a room
- **THEN** the application requests meters for that room and displays a loading state, an empty state, or each meter's type and status

#### Scenario: Create or edit a meter
- **WHEN** the landlord submits a meter with a selected room, meter type, and valid status
- **THEN** the system persists the meter and the application refreshes the room's meter list after success

#### Scenario: Reject an invalid meter
- **WHEN** the landlord submits a meter without a room or meter type, or with an invalid status
- **THEN** the system rejects the request and the application preserves the form with an actionable validation message

#### Scenario: Delete a meter
- **WHEN** the landlord confirms deletion of a meter
- **THEN** the system deletes the meter and its dependent readings according to the existing relationship policy, then removes it from the visible list

### Requirement: Manage meter readings
The system SHALL allow a landlord to list, create, edit, and delete readings for a selected meter, including reading time, current value, previous value, quantity, evidence URL, and note.

#### Scenario: Display meter reading history
- **WHEN** the landlord opens readings for a meter
- **THEN** the application displays readings ordered from newest to oldest with their reading time, current value, previous value, and quantity

#### Scenario: Create or edit a reading
- **WHEN** the landlord submits a reading with a meter, reading time, and non-negative numeric values
- **THEN** the system persists the reading and the application refreshes the reading history after success

#### Scenario: Reject invalid reading values
- **WHEN** the landlord submits a reading with a negative current value, previous value, or quantity, or omits the reading time
- **THEN** the system rejects the request and the application keeps the form open with a readable validation message

#### Scenario: Delete a reading
- **WHEN** the landlord confirms deletion of a reading that is not protected by an invoice reference
- **THEN** the system deletes the reading and removes it from the history

#### Scenario: Protect an invoiced reading
- **WHEN** the landlord attempts to delete a reading referenced by an invoice item
- **THEN** the system refuses the deletion and the application reports that the reading is already used for billing

### Requirement: Manage services assigned to a room
The system SHALL allow a landlord to view existing service-catalog entries for a room, assign an unassigned service, deactivate or reactivate an assignment, and remove an assignment when it is not needed.

#### Scenario: Display assigned services in room details
- **WHEN** the landlord opens room details
- **THEN** the application displays assigned services with their name, unit, calculation method, default unit price, and active state

#### Scenario: Assign an existing service
- **WHEN** the landlord selects an available service that is not currently assigned to the room
- **THEN** the system creates the room-service assignment and the application displays it as active

#### Scenario: Prevent duplicate assignment
- **WHEN** the landlord attempts to assign a service already assigned to the room
- **THEN** the system rejects the duplicate and the application reports that the service is already assigned

#### Scenario: Deactivate or reactivate a service
- **WHEN** the landlord changes the active state of an assigned service
- **THEN** the system updates `is_active` without discarding the assignment record and the application refreshes the displayed state

#### Scenario: Remove a service assignment
- **WHEN** the landlord confirms removal of an assigned service
- **THEN** the system removes the room-service relationship and the application removes it from the assigned-service list

### Requirement: Provide utility error and recovery states
The utility workflows SHALL show loading, empty, validation failure, successful mutation, not-found, conflict, and connectivity feedback in Vietnamese while keeping the user on a recoverable screen.

#### Scenario: Recover from a utility request failure
- **WHEN** a meter, reading, catalog, or room-service request fails
- **THEN** the application shows an actionable Vietnamese message and preserves the selected room or meter context for retry
