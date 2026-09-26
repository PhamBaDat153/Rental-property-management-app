## Purpose

Provide landlords with predictable, validated CRUD operations for the rental resources that connect locations, rooms, contracts, and tenants without exposing persistence relationships directly.

## ADDED Requirements

### Requirement: Manage locations
The system SHALL allow clients to create, list, retrieve, update, and delete locations through a resource API. A location create or update request MUST contain a non-null, non-blank unique location code, address line, province name, and valid status; optional ward, district, and description values MAY be null.

#### Scenario: Create a valid location
- **WHEN** a client submits all required location fields with a unique code
- **THEN** the system creates the location and returns a response containing its identifier, supplied values, status, and timestamps

#### Scenario: Reject invalid location input
- **WHEN** a client submits a null, blank, or oversized required location value, or an invalid/null status where status is required by the API
- **THEN** the system returns a validation error and does not persist the location

#### Scenario: Retrieve or update a location
- **WHEN** a client requests or updates an existing location identifier
- **THEN** the system returns the current location DTO or applies the validated replacement fields without serializing its rooms

#### Scenario: Reject unknown location
- **WHEN** a client requests, updates, or deletes a location identifier that does not exist
- **THEN** the system returns a not-found response

#### Scenario: Protect referenced location deletion
- **WHEN** a client deletes a location that is referenced by one or more rooms
- **THEN** the system returns a conflict response and leaves the location and rooms unchanged

### Requirement: Manage rooms
The system SHALL allow clients to create, list, retrieve, update, and delete rooms associated with an existing location. Room requests MUST validate non-null required values, non-negative area and rent values, positive maximum occupants, valid availability status, and a location identifier that exists. Room create and update requests MAY include image files; accepted images MUST be uploaded and represented by persisted image URLs in the room response.

#### Scenario: Create a valid room
- **WHEN** a client submits valid room fields and an existing location identifier
- **THEN** the system creates the room and returns a room DTO containing the location identifier without embedding the full location graph

#### Scenario: Reject invalid room input
- **WHEN** a client submits null required fields, a negative price or area, zero or negative maximum occupants, or an invalid relationship identifier
- **THEN** the system returns a validation or not-found error and does not persist the room

#### Scenario: Enforce room-code uniqueness within a location
- **WHEN** a client creates or updates a room using a room code already used by another room in the same location
- **THEN** the system rejects the request with a conflict response

#### Scenario: Protect referenced room deletion
- **WHEN** a client deletes a room referenced by a rental contract
- **THEN** the system returns a conflict response and leaves the room and contract data unchanged

#### Scenario: Upload room images
- **WHEN** a client submits valid room multipart data with one or more supported image files
- **THEN** the system uploads the files, persists their URLs for the room, and returns the room with its image URLs without exposing image entities

### Requirement: Manage rental contracts
The system SHALL allow clients to create, list, retrieve, update, and delete rental contracts associated with an existing room. Contract requests MUST validate a non-null room identifier, start date, non-negative monetary values, valid status, valid billing and payment values, and date relationships where an end date is not before the start date. Contract create and update requests MAY include one PDF or Word document; accepted documents MUST be uploaded and the resulting URL persisted as the contract document URL.

#### Scenario: Create a valid rental contract
- **WHEN** a client submits valid contract fields for an existing room
- **THEN** the system creates the contract and returns a contract DTO containing the room identifier and contract values

#### Scenario: Reject invalid contract input
- **WHEN** a client submits null required fields, negative monetary values, an invalid billing day, or an end date before the start date
- **THEN** the system returns a validation error and does not persist the contract

#### Scenario: Reject a missing contract room
- **WHEN** a client submits a contract with a room identifier that does not exist
- **THEN** the system returns a not-found error and does not persist the contract

#### Scenario: Protect contract deletion with dependent records
- **WHEN** a client deletes a contract that has tenant assignments or invoices
- **THEN** the system returns a conflict response and leaves the contract and dependent records unchanged

#### Scenario: Upload a contract document
- **WHEN** a client submits valid contract multipart data with a PDF or Word document
- **THEN** the system uploads the document and returns the contract with its persisted document URL

#### Scenario: Reject an unsupported contract document
- **WHEN** a client submits a contract document that is not PDF or Word format
- **THEN** the system returns a validation error and does not create or update the contract

### Requirement: Manage contract-tenant assignments
The system SHALL allow clients to add, retrieve, update, and remove tenant assignments for existing contracts and tenants. Assignment requests MUST require existing contract and tenant identifiers, validate move-in and move-out dates, and represent representative status explicitly.

#### Scenario: Add a valid contract tenant
- **WHEN** a client submits an existing contract identifier, existing tenant identifier, valid representative flag, and valid optional dates
- **THEN** the system creates the assignment and returns its composite identifiers and assignment values

#### Scenario: Reject an invalid assignment
- **WHEN** a client submits a null identifier, missing referenced contract or tenant, or a move-out date before move-in
- **THEN** the system returns a validation or not-found error and does not persist the assignment

#### Scenario: Prevent duplicate assignment
- **WHEN** a client adds a tenant already assigned to the same contract
- **THEN** the system returns a conflict response without creating a duplicate assignment

#### Scenario: Remove an assignment
- **WHEN** a client removes an existing contract-tenant assignment
- **THEN** the system deletes only that assignment and returns a successful no-content response

### Requirement: Return stable validation and relationship errors
The system SHALL return structured, client-readable error responses for malformed input, missing resources, duplicate values, relationship conflicts, and persistence failures. Successful resource responses MUST use DTOs and MUST NOT expose password hashes, recursive entity graphs, or internal persistence collections.

#### Scenario: Report malformed JSON or null input
- **WHEN** a client sends malformed JSON or omits a required request field
- **THEN** the system returns a client-error response identifying the invalid request without writing data

#### Scenario: Report relationship conflict
- **WHEN** a requested delete or update violates a current foreign-key relationship or domain guard
- **THEN** the system returns a conflict response with a stable error shape and keeps the affected data unchanged
