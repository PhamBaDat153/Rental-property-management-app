## 1. API Contracts And Validation

- [x] 1.1 Define request and response DTOs for Location, Room, RentalContract, and ContractTenant using relationship IDs instead of nested JPA entities.
- [x] 1.2 Add null, blank, size, enum, numeric-range, and date validation for every required request field; confirm malformed and missing fields produce the shared error shape.
- [x] 1.3 Define stable not-found, conflict, validation, and persistence error payloads without exposing entity internals or password fields.

## 2. Location And Room CRUD

- [x] 2.1 Complete Location repository/service/controller CRUD and map all supported location fields, including `description` where the schema exposes it.
- [x] 2.2 Add Location duplicate-code detection, missing-resource handling, and referenced-room deletion protection.
- [x] 2.3 Add Room repository/service/controller CRUD with existing-location validation and location-scoped room-code uniqueness.
- [x] 2.4 Add Room delete protection when rental contracts reference the room.
- [x] 2.5 Rename the room resource service to `RoomService` and `RoomServiceImplement`, and accept multipart room image uploads mapped to persisted `RoomImage` URLs.

## 3. Rental Contract And Assignment CRUD

- [x] 3.1 Add RentalContract repository/service/controller CRUD with room existence checks and contract field/date validation.
- [x] 3.2 Add contract delete protection for current tenant assignments and invoice dependencies.
- [x] 3.3 Add ContractTenant repository/service/controller operations using the composite contract/tenant identifier.
- [x] 3.4 Validate referenced contract and tenant existence, duplicate assignments, and move-in/move-out date ordering.
- [x] 3.5 Accept an optional multipart PDF or Word contract document, validate its type, upload it, and persist the returned document URL.

## 4. Verification

- [x] 4.1 Add service tests for valid create/update/delete flows, null input, invalid ranges, missing relationships, duplicate keys, and deletion conflicts.
- [x] 4.2 Add controller tests for HTTP status codes, request validation, DTO response shape, and non-recursive serialization.
- [x] 4.3 Run the backend test suite and verify the existing user/tenant management behavior remains compatible.
- [ ] 4.4 Exercise the CRUD endpoints against the development MySQL schema and verify database state after successful and rejected operations.
