## Purpose

Provide landlords with a complete maintenance-request workflow for tracking repair needs by room and tenant, including status progress, priority, validation, and Android management screens.

## ADDED Requirements

### Requirement: Manage maintenance requests
The system SHALL allow clients to create, list, retrieve, update, and delete maintenance requests associated with existing users and rooms, including title, description, priority, status, completion state, and supported request images.

#### Scenario: Create a valid maintenance request
- **WHEN** a client submits existing user and room identifiers, a non-blank title, description, valid priority, and valid status
- **THEN** the system creates the request and returns its identifiers, references, fields, timestamps, and image summaries

#### Scenario: Reject invalid maintenance input
- **WHEN** a client submits a missing relationship, blank title, unsupported priority/status, or invalid image data
- **THEN** the system returns a validation or not-found error and does not persist the request

#### Scenario: List maintenance requests
- **WHEN** a client requests maintenance records with optional room, status, or priority filters
- **THEN** the system returns request summaries ordered by newest creation time with readable status and priority labels

#### Scenario: Update maintenance progress
- **WHEN** a client updates the title, description, priority, status, or completion information of an existing request
- **THEN** the system persists the update and sets completion metadata consistently when the request becomes completed

#### Scenario: Delete maintenance request safely
- **WHEN** a client confirms deletion of an existing maintenance request
- **THEN** the system deletes the request and owned images or returns a stable conflict/error response without partial deletion

### Requirement: Connect maintenance workflow to Android UI
The Android application SHALL provide a Maintenance screen reachable from the utilities area with list, filter, create, edit, detail, status update, loading, empty, error, and confirmed-delete states.

#### Scenario: Open maintenance management
- **WHEN** the user selects Bảo trì from the utilities screen
- **THEN** the application opens the maintenance list with Vietnamese labels for status and priority

#### Scenario: Update maintenance status from Android
- **WHEN** the user changes a valid maintenance request status
- **THEN** the application sends the update, refreshes the request, and displays the resulting status text

#### Scenario: Maintenance API failure
- **WHEN** a maintenance request fails due to validation, missing room/user, conflict, or connectivity
- **THEN** the application keeps the user on the current screen and displays actionable Vietnamese feedback
