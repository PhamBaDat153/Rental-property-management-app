## Why

The backend contains persistence models and a partial Location API, but landlords do not yet have a consistent CRUD API for locations, rooms, rental contracts, or contract-tenant assignments. Completing these APIs now establishes the basic rental-management workflow while keeping invalid null or missing input out of persistence.

## What Changes

- Complete Location CRUD with request/response DTOs, validation, and consistent not-found handling.
- Add Room CRUD scoped to an existing Location.
- Add RentalContract CRUD scoped to an existing Room.
- Add ContractTenant management through validated contract-tenant request/response DTOs.
- Validate required fields, identifiers, dates, numeric amounts, enum values, and relationship references before persistence.
- Prevent deletion of locations, rooms, tenants, or contracts when current database relationships would make the deletion invalid.
- Return stable response DTOs without serializing bidirectional JPA relationships recursively.
- Add backend tests for valid CRUD, null/invalid input, missing references, duplicate values, and deletion guards.

## Capabilities

### New Capabilities
- `rental-resource-crud`: Basic CRUD management for locations, rooms, rental contracts, and their tenant assignments.

### Modified Capabilities

None.

## Impact

- Spring backend controllers, services, repositories, DTOs, validation, exception handling, and entity mappings under `Source/BE`.
- Existing Location API behavior and endpoint contracts.
- Rental relationship enforcement across `Location`, `Room`, `RentalContract`, `ContractTenant`, and `Tenant`.
- Backend unit and web-layer tests; no new runtime dependency is required.
