## MODIFIED Requirements

### Requirement: Manage locations in the properties screen
The Android application SHALL replace the placeholder `propertiesFragment` content with a Location management view that lists locations and navigates to a dedicated Location detail screen for create, detail, edit, and delete actions.

#### Scenario: Display the location list
- **WHEN** the user opens the properties screen and selects the Location view
- **THEN** the application requests the available locations and displays each location code, address information, status, and a clear row interaction

#### Scenario: Create a location
- **WHEN** the user submits a valid structured Location form
- **THEN** the application sends the supported Location request and refreshes the list after a successful response

#### Scenario: View and edit a location
- **WHEN** the user opens a Location detail screen and chooses edit
- **THEN** the application displays the current editable Location fields, preserves valid input during validation errors, and sends an update only after the form is valid

#### Scenario: Open Location details
- **WHEN** the user taps a Location row
- **THEN** the application navigates to `LocationDetailFragment` with the selected Location identifier

#### Scenario: Delete a location safely
- **WHEN** the user confirms deletion from the Location detail screen
- **THEN** the application requires confirmation, sends the delete request, and reports a relationship conflict when the backend refuses deletion because rooms reference the location

### Requirement: Manage rooms in the properties screen
The Android application SHALL provide a Room management view with list and navigation to a dedicated Room detail screen, using the existing Room resource contract and an existing Location as the room relationship.

#### Scenario: Display the room list
- **WHEN** the user selects the Room view
- **THEN** the application displays each room name, room code, location identifier or address context, rent price, and status

#### Scenario: Display an unnamed room
- **WHEN** a room has a null or blank `room_name`
- **THEN** the application displays `Chưa đặt tên phòng` as the name and displays the room code only as supporting metadata, never as the room name

#### Scenario: Open Room details
- **WHEN** the user taps a Room row
- **THEN** the application navigates to `RoomDetailFragment` with the selected Room identifier

#### Scenario: Create or edit a room
- **WHEN** the user submits valid Room fields with an existing Location selected and zero to six images
- **THEN** the application sends the Room request using the backend's multipart resource contract and refreshes or updates the visible Room data after success

#### Scenario: Delete a room safely
- **WHEN** the user confirms deletion from the Room detail screen
- **THEN** the application sends the delete request and returns to the appropriate list state after success, or reports a relationship conflict when a rental contract references the Room

### Requirement: Provide explicit resource states and Vietnamese feedback
The resource views and detail forms SHALL represent loading, empty, request failure, validation failure, successful mutation, and destructive-action confirmation states with readable Vietnamese labels and without relying on color alone.

#### Scenario: Show a loading or empty state
- **WHEN** a list or detail request is in progress or succeeds with no records/images
- **THEN** the application shows the corresponding loading or empty message and does not present stale rows as current data

#### Scenario: Report API failure
- **WHEN** a resource request fails because of connectivity, validation, not-found, duplicate-code, upload, or relationship-conflict errors
- **THEN** the application shows an actionable Vietnamese message and keeps the user on a recoverable screen
