## Why

The Android `propertiesFragment` is still a placeholder, so landlords cannot manage the Location and Room records already exposed by the backend. This change adds the first usable rental-resource screen while keeping the existing User management visual language and the confirmed domain rules: locations have no separate name, and a missing room name must not be replaced by the room code.

## What Changes

- Replace the `propertiesFragment` placeholder with a Room and Location management screen.
- Add Location list, detail, create, edit, delete, and filtering by location code or address.
- Add Room list, detail, create, edit, delete, and filtering by `room_name`.
- Display an unnamed room as `Chưa đặt tên phòng` with its `room_code` as supporting metadata.
- Add Android DTOs, Retrofit contracts, adapters, validation, loading/empty/error states, and navigation destinations required by the UI.
- Use the existing Location and Room backend endpoints without changing their resource semantics.
- Guard destructive actions and surface backend not-found/conflict/validation errors in Vietnamese.

## Capabilities

### New Capabilities

- `rental-resource-ui`: Android management UI for Location and Room resources, including list, detail, filtering, and CRUD interactions.

### Modified Capabilities

- None.

## Impact

- Android Java fragments, DTOs, Retrofit service declarations, adapters, XML layouts, drawables/styles if required, and Navigation Component configuration under `Source/Rentaly_Management/app`.
- Existing backend contracts under `/be/locations` and `/be/rooms`; no backend endpoint or database schema change is planned.
- `propertiesFragment` navigation behavior changes from the generic `ScreenFragment` placeholder to the resource-management screen.
