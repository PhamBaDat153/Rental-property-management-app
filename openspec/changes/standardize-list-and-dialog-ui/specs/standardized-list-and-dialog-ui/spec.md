## Purpose

Provide a coherent, accessible Android visual experience across rental-management list fragments and dialogs while preserving all existing domain workflows and data behavior.

## ADDED Requirements

### Requirement: Standardize management list presentation
The Android application SHALL use a consistent User-style presentation for management list fragments, including a clear contextual header, accessible back and primary actions, readable list rows, consistent surfaces and spacing, and explicit loading, empty, and error states.

#### Scenario: Open a management list
- **WHEN** the user opens a Service, Meter, Invoice, Maintenance, Announcement, Properties, Contracts, or User management list
- **THEN** the screen presents the same hierarchy of contextual header, content surface, list rows, and state feedback while retaining the screen's domain-specific title and actions

#### Scenario: Show list loading or empty state
- **WHEN** a list is loading or succeeds with no records
- **THEN** the screen shows a readable loading or Vietnamese empty state without presenting stale rows as current data

#### Scenario: Show list failure
- **WHEN** a list request fails
- **THEN** the screen shows an actionable Vietnamese error or retry path using the same visual treatment as other management lists

### Requirement: Standardize create and edit dialogs
The Android application SHALL present remaining create/edit dialogs with the User dialog visual language, including consistent outer padding, helper text, field labels, semantic colors, input surfaces, spacing, and controls with at least 48dp touch targets.

#### Scenario: Open a standardized dialog
- **WHEN** the user creates or edits a Service, Meter, MeterReading, Invoice, Maintenance, Announcement, Location, Room, RentalContract, or ContractTenant record
- **THEN** the form uses readable labels, consistent User-style spacing and inputs, explicit cancel/save actions, and domain-appropriate native controls

#### Scenario: Use a long dialog on a small screen
- **WHEN** a dialog contains more content than the available phone viewport
- **THEN** the user can scroll through all fields and reach the actions without clipping or losing entered values

### Requirement: Preserve behavior and accessibility during visual standardization
Visual standardization SHALL preserve existing field IDs, domain fields, validation, API requests, navigation, file/image actions, and mutation feedback while meeting the UI guide's accessibility requirements.

#### Scenario: Submit a valid form
- **WHEN** the user submits valid data from a standardized dialog
- **THEN** the application performs the same create or update operation and success refresh as before

#### Scenario: Recover from invalid input or request failure
- **WHEN** validation or a request fails
- **THEN** the user remains on a recoverable form or screen with Vietnamese feedback and valid entered values preserved where the existing flow supports it

#### Scenario: Use icon-only controls
- **WHEN** the screen presents an icon-only back or action control
- **THEN** the control has a meaningful content description, readable contrast, and a touch target of at least 48dp
