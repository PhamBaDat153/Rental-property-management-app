## Why

Rooms currently have no landlord workflow for configuring room services or recording utility consumption. The backend already contains the `meter`, `meter_reading`, `service`, and `room_service` data model, but it exposes none of this behavior to the Android application. This change adds the missing end-to-end workflow while keeping room service assignment in the existing room detail screen.

## What Changes

- Add backend APIs and application services for meters and meter readings scoped to a room.
- Add backend APIs for listing the existing service catalog and assigning, deactivating, reactivating, or removing services for a room.
- Add Android DTOs and Retrofit endpoints for meters, readings, services, and room-service assignments.
- Add a dedicated `MeterFragment` reached with a `room_id` from room details.
- Add meter CRUD and meter-reading CRUD UI, including validation for non-negative values and reading history.
- Add a room-service management section to `RoomDetailFragment` for assigning existing catalog services and changing their active state.
- Preserve inactive `room_service` rows when a service is disabled so the assignment history is not discarded.
- Add navigation, layouts, tests, and Vietnamese loading, empty, validation, success, and error feedback for the new workflows.

## Capabilities

### New Capabilities

- `room-utilities`: Manage room meters, meter readings, and service assignments from Android with supporting backend APIs.

### Modified Capabilities

- `rental-resource-ui`: Extend room detail behavior with room-service assignment controls and navigation to room utility management.

## Impact

- Spring Boot backend model, repositories, DTOs, services, controllers, validation, and exception handling under `Source/BE`.
- Android navigation, `RoomDetailFragment`, new `MeterFragment`, layouts, DTOs, Retrofit API declarations, and unit tests under `Source/Rentaly_Management/app`.
- Existing database tables are reused; no new schema tables are required. The implementation must respect the existing `is_active` field on `room_service` and the foreign-key relationships to rooms, meters, readings, and services.
- Existing location and room list workflows remain unchanged except for the room-detail entry point to utilities.
