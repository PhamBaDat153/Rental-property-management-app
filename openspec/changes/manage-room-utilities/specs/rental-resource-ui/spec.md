## MODIFIED Requirements

### Requirement: Manage rooms in the properties screen
The Android application SHALL provide a Room management view with list, detail, create, edit, and delete actions using the existing Room resource contract and an existing Location as the room relationship. The room detail view SHALL expose the assigned services for that room and provide an entry point to manage the room's meters and meter readings.

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

#### Scenario: View room services and utility entry point
- **WHEN** the user opens a room detail
- **THEN** the application displays the room's assigned services and provides a clear action that opens utility management with that room selected

#### Scenario: Manage assigned services from room detail
- **WHEN** the user assigns, deactivates, reactivates, or removes a room service from room detail
- **THEN** the application sends the corresponding room-service request, keeps the room detail screen open, and refreshes the assigned-service state after success
