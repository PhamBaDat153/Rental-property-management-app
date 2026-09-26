# rental-resource-ui Specification

## Purpose

Provide landlords with a clear Android workflow for viewing and maintaining locations and rooms through the existing rental-resource APIs, without exposing database relationships or inventing unsupported domain fields.

## Requirements

### Requirement: Manage locations in the properties screen
The Android application SHALL replace the placeholder `propertiesFragment` content with a Location management view that lists locations and exposes create, detail, edit, and delete actions.

#### Scenario: Display the location list
- **WHEN** the user opens the properties screen and selects the Location view
- **THEN** the application requests the available locations and displays each location code, address information, status, and a clear row interaction

#### Scenario: Create a location
- **WHEN** the user submits a location with the required code, address line, province, and valid status
- **THEN** the application sends the supported Location request and refreshes the list after a successful response

#### Scenario: View and edit a location
- **WHEN** the user opens a location row and chooses edit
- **THEN** the application displays the current editable location fields, preserves valid input during validation errors, and sends an update only after the form is valid

#### Scenario: Delete a location safely
- **WHEN** the user chooses to delete a location
- **THEN** the application requires confirmation, sends the delete request after confirmation, and reports a relationship conflict when the backend refuses deletion because rooms reference the location

### Requirement: Filter locations by code or address
The Location view SHALL filter the displayed locations by a case-insensitive query matching the location code or any displayed address component, including address line, ward, district, or province.

#### Scenario: Filter locations
- **WHEN** the user enters a search query in the Location filter
- **THEN** only locations whose code or address fields contain the query remain visible, and clearing the query restores the complete loaded list

### Requirement: Manage rooms in the properties screen
The Android application SHALL provide a Room management view with list, detail, create, edit, and delete actions using the existing Room resource contract and an existing Location as the room relationship.

#### Scenario: Display the room list
- **WHEN** the user selects the Room view
- **THEN** the application displays each room name, room code, location identifier or address context, rent price, and status

#### Scenario: Display an unnamed room
- **WHEN** a room has a null or blank `room_name`
- **THEN** the application displays `Chưa đặt tên phòng` as the name and displays the room code only as supporting metadata, never as the room name

#### Scenario: Create or edit a room
- **WHEN** the user submits valid room fields with an existing Location selected
- **THEN** the application sends the Room request using the backend's multipart resource contract and refreshes or updates the visible room data after success

#### Scenario: Delete a room safely
- **WHEN** the user confirms deletion of a room
- **THEN** the application sends the delete request and returns to the appropriate list state after success, or reports a relationship conflict when a rental contract references the room

### Requirement: Filter rooms by room name
The Room view SHALL filter rooms by a case-insensitive query matching `room_name` only.

#### Scenario: Filter rooms by name
- **WHEN** the user enters a query in the Room filter
- **THEN** only rooms whose non-blank `room_name` contains the query remain visible, and unnamed rooms do not match arbitrary queries through their room code

### Requirement: Provide explicit resource states and Vietnamese feedback
The resource views SHALL represent loading, empty, request failure, validation failure, successful mutation, and destructive-action confirmation states with readable Vietnamese labels and without relying on color alone.

#### Scenario: Show a loading or empty state
- **WHEN** a list request is in progress or succeeds with no records
- **THEN** the application shows the corresponding loading or empty message and does not present stale rows as current data

#### Scenario: Report API failure
- **WHEN** a resource request fails because of connectivity, validation, not-found, duplicate-code, or relationship-conflict errors
- **THEN** the application shows an actionable Vietnamese message and keeps the user on a recoverable screen
