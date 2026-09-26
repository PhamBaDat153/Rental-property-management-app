## Purpose

Provide structured address entry, image-aware room forms, and dedicated detail screens so landlords can review and maintain rental resources comfortably on Android devices.

## ADDED Requirements

### Requirement: Select Vietnamese address names from the province API
The Location create and edit forms SHALL use `https://provinces.open-api.vn/api/v2/` to provide dependent province, district, and ward choices, while submitting the selected names into the existing `province_name`, `district_name`, and `ward_name` fields.

#### Scenario: Load province choices
- **WHEN** the user opens a Location form
- **THEN** the application loads province choices from the v2 API and presents them in a labeled selector

#### Scenario: Load dependent district and ward choices
- **WHEN** the user selects a province or district
- **THEN** the application loads and limits the next selector to the corresponding districts or wards, clearing lower-level selections that are no longer valid

#### Scenario: Handle address API failure
- **WHEN** the province API is unavailable or returns invalid data
- **THEN** the application displays a recoverable error and does not silently submit an unselected required address value

### Requirement: Use structured XML forms for resource creation and editing
Location and Room create/edit flows SHALL use labeled, scrollable forms that preserve valid input when validation fails and prevent duplicate submission while a request is in progress.

#### Scenario: Validate a Location form
- **WHEN** the user submits a Location form without a code, address line, province, or valid status
- **THEN** the application identifies the invalid field, keeps entered values, and does not send the request

#### Scenario: Validate a Room form
- **WHEN** the user submits a Room form with missing required values, an invalid Location, a negative area or rent, or a non-positive occupant limit
- **THEN** the application identifies the validation problem, keeps entered values, and does not send the request

### Requirement: Select and upload Room images
The Room create and edit forms SHALL allow selection of no more than six supported image files and send selected images through the existing Room multipart contract.

#### Scenario: Create a room with images
- **WHEN** the user selects between one and six valid images and submits a valid Room form
- **THEN** the application includes the images in the multipart request and displays the returned image URLs after success

#### Scenario: Reject a seventh image
- **WHEN** the user attempts to select more than six images for one Room
- **THEN** the application rejects or truncates the additional selection with a clear message and keeps the first six selected images

#### Scenario: Keep existing images on edit without new selection
- **WHEN** the user edits a Room and submits without selecting new images
- **THEN** the application sends no replacement image parts and the existing Room images remain unchanged

#### Scenario: Remove an invalid image selection
- **WHEN** the user selects a non-image file or an unreadable image URI
- **THEN** the application excludes that file and explains that only readable image files are supported

### Requirement: Provide dedicated Location and Room detail screens
The application SHALL provide `LocationDetailFragment` and `RoomDetailFragment` destinations that load a resource by identifier and expose view, edit, and confirmed delete actions.

#### Scenario: Open Location details
- **WHEN** the user taps a Location row
- **THEN** the application navigates to `LocationDetailFragment`, loads the selected Location, and displays its code, address, status, and description

#### Scenario: Open Room details
- **WHEN** the user taps a Room row
- **THEN** the application navigates to `RoomDetailFragment`, loads the selected Room, and displays its identity, Location context, room values, status, description, and available images

#### Scenario: Edit from a detail screen
- **WHEN** the user chooses edit from either detail screen and saves valid changes
- **THEN** the application returns to refreshed detail data after a successful update and preserves the detail screen on recoverable failure

#### Scenario: Delete from a detail screen
- **WHEN** the user confirms deletion from either detail screen
- **THEN** the application sends the delete request and navigates back to the resource list after success, or keeps the detail context and reports the backend conflict on failure

### Requirement: Present Room images accessibly
Room detail SHALL present selected Room images in a readable gallery or vertically scrollable image list, provide meaningful content descriptions, and show a labeled empty state when no images exist.

#### Scenario: Display a Room image gallery
- **WHEN** a Room response contains image URLs
- **THEN** the detail screen displays each available image with a meaningful content description and keeps the rest of the Room information reachable

#### Scenario: Display no-image state
- **WHEN** a Room response contains no image URLs
- **THEN** the detail screen displays a text state such as `Chưa có hình ảnh phòng` rather than an empty or inaccessible image container
