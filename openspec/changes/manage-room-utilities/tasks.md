## 1. Backend Persistence And Contracts

- [x] 1.1 Add repositories for `Meter`, `MeterReading`, `Service`, and `RoomService`, including room/meter filtering, newest-reading ordering, and composite-assignment lookup; verify the project compiles.
- [x] 1.2 Add request/response DTOs with UUID references, enum/status fields, `BigDecimal` values, and validation constraints; verify invalid negative readings and missing required fields are rejected by validation tests.
- [x] 1.3 Add meter and meter-reading application services with room/meter existence checks, CRUD operations, reading ordering, and conflict handling for invoice-protected readings; verify service unit tests cover not-found, invalid, and delete-conflict paths.
- [x] 1.4 Add room-service catalog and assignment services that list catalog entries, prevent duplicate assignment, update `is_active`, and remove assignments; verify duplicate and missing-reference tests pass.

## 2. Backend HTTP API

- [x] 2.1 Add meter endpoints for room-scoped list, detail, create, update, and delete; verify controller tests assert status codes, room scoping, and Vietnamese/client-readable error responses.
- [x] 2.2 Add meter-reading endpoints for meter-scoped list, detail, create, update, and delete; verify responses are newest-first and protected deletions return a conflict.
- [x] 2.3 Add service catalog and room-service assignment endpoints; verify assign, deactivate, reactivate, remove, and duplicate-assignment flows through controller/integration tests.
- [x] 2.4 Run the backend test suite and verify existing location, room, contract, and contract-tenant tests remain green.

## 3. Android API And Models

- [x] 3.1 Add Android DTOs for meter, meter request, meter reading, reading request, service, room-service assignment, and active-state request; verify Gson serialization/deserialization for UUID, date-time, enum, and decimal fields.
- [x] 3.2 Add Retrofit methods for all meter, reading, service-catalog, and room-service endpoints; verify request paths, query parameters, composite IDs, and request bodies with API contract tests or inspection tests.

## 4. Room Detail Service Management

- [x] 4.1 Add the room-service section and utility-management action to `fragment_room_detail.xml`; verify the layout has accessible labels, empty/loading placeholders, and actions reachable without leaving room details.
- [x] 4.2 Load and render assigned services in `RoomDetailFragment`, including active state, unit, calculation method, and price; verify a room detail response refreshes the service section.
- [x] 4.3 Add assign, deactivate/reactivate, and remove interactions using confirmation where destructive; verify duplicate, empty-catalog, validation, conflict, and connectivity feedback keeps the user on the room detail screen.

## 5. Meter Fragment

- [x] 5.1 Add `MeterFragment` navigation with required `room_id` argument and a dedicated layout; verify opening from `PropertiesFragment` after selecting a room preserves the selected room.
- [x] 5.2 Implement room meter loading, empty/loading/error states, and meter create/edit/delete dialogs; verify valid mutations refresh the meter list and invalid input remains recoverable.
- [x] 5.3 Implement meter reading history, reading create/edit/delete dialogs, newest-first display, non-negative validation, and invoice-protected delete feedback; verify reading mutations refresh only the selected meter context.
- [x] 5.4 Add Android unit tests for meter/readings validation, display formatting, and selected-room/selected-meter request behavior.

## 6. Integration Verification

- [x] 6.1 Run backend and Android test suites and verify all existing tests plus new utility tests pass.
- [ ] 6.2 Perform an end-to-end smoke check with an existing room and service catalog: assign a service, deactivate/reactivate it, create a meter, create a reading, and confirm the data remains after reopening both screens.
- [x] 6.3 Run OpenSpec validation and verify the implementation references every requirement in `room-utilities` and the modified `rental-resource-ui` delta.
