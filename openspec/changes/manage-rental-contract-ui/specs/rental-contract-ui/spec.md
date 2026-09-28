## Purpose

Provide landlords with a clear Android workflow to find, create, inspect, update, and safely remove rental contracts while attaching and accessing the contract's PDF document.

## ADDED Requirements

### Requirement: Display and filter rental contracts
The Android application SHALL provide a contracts screen that lists rental contracts with the related room code, contract period, rent amount, and status. The screen SHALL load rooms alongside contracts and match records by room identifier so that filtering is performed by the related room code on the client.

#### Scenario: Display contracts with room context
- **WHEN** the user opens the contracts screen and both contract and room data are available
- **THEN** each contract row shows its related room code, period, rent amount, and a text status label

#### Scenario: Filter by room code
- **WHEN** the user enters a query in the contract filter
- **THEN** only contracts whose related room code contains the case-insensitive query remain visible

#### Scenario: Contract references an unavailable room
- **WHEN** a contract references a room that is not present in the loaded room list
- **THEN** the contract remains identifiable with a `Chưa xác định phòng` label and does not match a room-code query unless the query is empty

#### Scenario: Show contract collection states
- **WHEN** contract data is loading, empty, unavailable, or fails to load
- **THEN** the screen shows a clear Vietnamese loading, empty, unavailable, or retryable error state without presenting misleading stale actions

### Requirement: Create and edit rental contracts
The Android application SHALL provide labeled create and edit forms for the supported contract fields: an existing room, start date, optional end date, rent amount, deposit, optional billing day, payment due days, status, optional terms, optional termination date, optional termination reason, and an optional PDF or DOCX document. Required and invalid values SHALL be identified near the affected field and valid inputs SHALL be preserved after validation or request failure.

#### Scenario: Create a valid contract without a document
- **WHEN** the user submits a valid contract with an existing room and no PDF selected
- **THEN** the application sends the supported contract multipart request without a document part and refreshes the contract list after success

#### Scenario: Create a valid contract with a PDF
- **WHEN** the user selects one PDF and submits valid contract fields
- **THEN** the application sends the contract request and PDF as multipart data and displays the returned document as available after success

#### Scenario: Reject invalid contract form data
- **WHEN** required values are missing, monetary values are negative, payment values are outside their allowed ranges, or the end date precedes the start date
- **THEN** the application keeps the form open, identifies the invalid input in Vietnamese, preserves valid values, and does not submit the request

#### Scenario: Edit a contract without replacing its document
- **WHEN** the user updates valid contract fields without selecting a new PDF
- **THEN** the application submits no new document part and preserves the currently displayed document

#### Scenario: Room data is unavailable for form selection
- **WHEN** rooms cannot be loaded for a create or edit form
- **THEN** the application explains that a room is unavailable, prevents submission without a valid room, and offers a recoverable path

### Requirement: View and manage contract details
The Android application SHALL provide a dedicated detail screen that groups contract identity, room context, dates, financial values, payment settings, status, terms, and lifecycle metadata. The screen SHALL allow editing supported values, including signed date, termination date, and termination reason, and SHALL provide update and confirmed delete actions.

#### Scenario: View contract details
- **WHEN** the user selects a contract row
- **THEN** the application opens a scrollable detail screen showing the contract identity before grouped secondary information and representing missing values as `Chưa cập nhật`

#### Scenario: Update a contract from detail
- **WHEN** the user submits valid edited values, including any changed signed date, termination date, or termination reason, from the detail screen
- **THEN** the application updates the contract and shows the saved values without leaving stale editable data presented as current

#### Scenario: Replace a contract document from detail
- **WHEN** the user selects a valid PDF or DOCX replacement and submits the detail form
- **THEN** the application sends the replacement as the optional document multipart part and displays the returned document as current

#### Scenario: Delete a contract safely
- **WHEN** the user chooses to delete a contract
- **THEN** the application shows a specific confirmation dialog and only deletes after confirmation

#### Scenario: Delete is blocked by dependent records
- **WHEN** the backend rejects deletion because the contract has tenant assignments or invoices
- **THEN** the application remains on a recoverable screen and explains in Vietnamese that dependent records prevent deletion

### Requirement: Access contract documents
The Android application SHALL provide document actions when a contract has a PDF or DOCX document URL. Viewing SHALL hand the URL to an installed compatible application or browser, and downloading SHALL use the Android platform download facility without embedding a document viewer dependency.

#### Scenario: Open an available contract document
- **WHEN** the user chooses to view a contract document with a valid URL
- **THEN** the application opens the URL through a supported PDF application or browser

#### Scenario: Download an available contract document
- **WHEN** the user chooses to download a contract document with a valid URL
- **THEN** the application starts a native download and reports the download action without blocking the detail screen

#### Scenario: Document action is unavailable
- **WHEN** a contract has no document URL or the device has no handler for the view action
- **THEN** the application disables or hides the unavailable action and shows an actionable Vietnamese message when the user attempts it
