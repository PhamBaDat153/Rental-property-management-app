## Context

The backend already maps `Meter`, `MeterReading`, `Service`, and `RoomService` to the existing database tables. It does not currently expose repositories, DTOs, application services, or controllers for those entities. The Android app has a `RoomDetailFragment` for one room and a Retrofit `ApiService` that currently covers locations, rooms, contracts, and contract tenants. The existing `room_service.is_active` column provides the required persisted active/inactive state.

## Goals / Non-Goals

**Goals:**

- Add room-scoped REST operations for meter and meter-reading CRUD.
- Add room-service assignment and active-state operations using the existing service catalog.
- Add a dedicated Android `MeterFragment` that receives `room_id`, manages meters, and exposes readings for each meter.
- Keep room-service controls inside `RoomDetailFragment` and avoid changing the existing location/room list screen.
- Reuse the current exception mapping, Retrofit setup, UUID fields, `BigDecimal` values, and simple XML/View-based UI patterns.

**Non-Goals:**

- Creating or editing the global service catalog in this change.
- Adding invoice generation, automatic billing, or tariff calculation.
- Adding new database tables or changing existing column meanings.
- Moving meter management into the bottom navigation or combining it with the properties list.

## Decisions

### Backend resource boundaries

Use separate controllers and application services for meters, readings, and room-service assignments. Meters are queried by `roomId`; readings are queried by `meterId`; room-service endpoints are nested under a room. This keeps foreign-key validation close to the operation and prevents the Android client from submitting arbitrary entity graphs.

Proposed endpoints:

```text
GET    /be/meters?roomId={roomId}
GET    /be/meters/{id}
POST   /be/meters
PUT    /be/meters/{id}
DELETE /be/meters/{id}

GET    /be/meters/{meterId}/readings
GET    /be/meter-readings/{id}
POST   /be/meter-readings
PUT    /be/meter-readings/{id}
DELETE /be/meter-readings/{id}

GET    /be/services
GET    /be/rooms/{roomId}/services
POST   /be/rooms/{roomId}/services/{serviceId}
PUT    /be/rooms/{roomId}/services/{serviceId}
DELETE /be/rooms/{roomId}/services/{serviceId}
```

Request and response DTOs expose IDs rather than nested JPA objects. Backend services validate referenced room, meter, and service records, reject duplicate room-service assignments, and translate missing records to the existing 404 response. Database foreign keys and the existing exception handler remain the final integrity boundary.

### Reading validation and ordering

Reject negative `current_value`, `previous_value`, and `quantity` values at request validation and service boundaries. Do not silently calculate `quantity` in this change because the schema stores it explicitly and some installations may use a billing-specific calculation. Reading list queries return newest `reading_at` first; ties use `reading_id` for stable ordering.

### Room-service lifecycle

Assignment creation uses the composite `(room_id, service_id)` key. Repeated assignment returns a conflict. `PUT` changes only `is_active`, allowing a service to be disabled and reactivated without deleting its historical relationship. `DELETE` removes the relationship only when the landlord explicitly chooses removal. This distinguishes temporary deactivation from permanent unassignment.

### Android navigation and UI

Add a utility-management action to `RoomDetailFragment` and navigate to a new `MeterFragment` with the selected room UUID. `MeterFragment` loads the room, meters, and readings on demand, using a simple room header, meter list, and reading dialog/list rather than introducing a new adapter framework. Meter CRUD and reading CRUD can use dialogs consistent with the current detail screens.

Add a room-service section to `fragment_room_detail.xml`. It lists assigned services and includes an action that loads catalog services, filters out currently assigned services, and assigns the selected one. Active state changes use an explicit control and refresh the section after mutation. All failures remain on the current room or meter context and use Vietnamese feedback.

### Verification approach

Backend tests cover repository/service/controller behavior for room scoping, duplicate assignments, negative readings, ordering, and protected reading deletion. Android unit tests cover meter/reading validation and filtering of assignable services; an instrumentation or manual integration check verifies navigation with `room_id`, loading states, mutations, and refresh behavior.

## Risks / Trade-offs

- [Risk] Deleting a meter cascades to readings because the existing schema uses `ON DELETE CASCADE` → require confirmation in the UI and refresh the meter list after deletion.
- [Risk] A reading may already be referenced by an invoice item → reject deletion through the existing data-integrity/conflict path and show billing-specific feedback.
- [Risk] Existing service catalog data may be empty → show an explicit empty state and keep the room detail screen usable; do not create catalog entries implicitly.
- [Risk] The Android app currently uses compact fragments and inline callbacks → keep the feature within existing patterns rather than adding a new state-management dependency.
- [Risk] Multiple requests can finish out of order after a mutation → refresh the relevant list only after the mutation response and retain the selected room/meter IDs.

## Migration Plan

1. Deploy backend endpoints and Android changes against the existing tables; no schema migration is required.
2. Verify existing rooms and service catalog records before enabling the utility entry point.
3. If rollback is needed, remove or disable the new navigation action and Android calls; existing room, service, and invoice behavior remains unaffected.
