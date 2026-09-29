## Why

The Announcement dialog currently loads every tenant into one unfiltered recipient list. Landlords must scroll through all users to find recipients, cannot target tenants by room, and have no safe way to select all currently relevant recipients without manually checking each row.

## What Changes

- Add Android-only recipient filtering by tenant name and room.
- Load the existing rooms, rental contracts, contract-tenant assignments, and tenant profiles to derive room membership locally.
- Display each tenant once, with all related room labels grouped on the same recipient row.
- Add a room selector, name search, and `Chọn tất cả` behavior scoped to the currently visible filtered recipients.
- Preserve the existing `recipient_ids` request payload and announcement backend APIs.
- Keep create/edit announcement behavior, validation, send/draft status, and User-style dialog presentation intact.

## Capabilities

### New Capabilities

- `announcement-recipient-filtering`: Local Android filtering and bulk selection of announcement recipients by tenant name and related rooms.

### Modified Capabilities

- None. The existing announcement API contract remains unchanged; this adds client-side recipient-selection behavior.

## Impact

- Android `AnnouncementFragment` and announcement dialog layout.
- Android DTO/API loading flow for existing UserTenant, Room, RentalContract, and ContractTenant data.
- No backend, database, endpoint, request DTO, or external dependency changes.
