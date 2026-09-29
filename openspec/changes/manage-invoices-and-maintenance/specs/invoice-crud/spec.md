## Purpose

Provide landlords with a validated invoice workflow that exposes billing records and line items through stable APIs and usable Android screens without leaking persistence graphs.

## ADDED Requirements

### Requirement: Manage invoices and invoice items
The system SHALL allow clients to create, list, retrieve, update, and delete invoices associated with an existing rental contract, including validated invoice items linked to existing services and optional meter readings.

#### Scenario: Create a valid invoice
- **WHEN** a client submits an existing contract, unique invoice number, valid dates, non-negative monetary totals, status, and valid line items
- **THEN** the system creates the invoice and returns its identifiers, totals, status, dates, note, and item summaries

#### Scenario: Reject invalid invoice input
- **WHEN** a client submits a missing contract, blank or duplicate invoice number, negative amount, invalid date, missing item service, or negative item amount
- **THEN** the system returns a validation or conflict error and does not persist the invoice

#### Scenario: List invoices
- **WHEN** a client requests invoices with optional contract or status filters
- **THEN** the system returns invoice summaries ordered by newest issue date and includes readable status and total values

#### Scenario: Update invoice
- **WHEN** a client submits valid replacement invoice fields and line items for an existing invoice
- **THEN** the system updates the invoice and returns the current invoice representation

#### Scenario: Delete invoice safely
- **WHEN** a client confirms deletion of an existing invoice
- **THEN** the system deletes the invoice and its owned line items, or returns a conflict when domain dependencies prevent deletion

### Requirement: Connect invoice workflow to Android UI
The Android application SHALL provide an Invoice screen reachable from the utilities area with loading, empty, error, list, detail/edit, create, validation, success, and confirmed-delete states.

#### Scenario: Open invoice management
- **WHEN** the user selects Hóa đơn from the utilities screen
- **THEN** the application opens the invoice list and does not route the user through meter management

#### Scenario: Create or edit invoice from Android
- **WHEN** the user submits valid invoice and item fields
- **THEN** the application sends the invoice request, displays success feedback, and refreshes the affected list or detail state

#### Scenario: Invoice API failure
- **WHEN** an invoice request fails due to validation, missing relationship, duplicate number, or connectivity
- **THEN** the application shows actionable Vietnamese feedback and preserves the recoverable form state
