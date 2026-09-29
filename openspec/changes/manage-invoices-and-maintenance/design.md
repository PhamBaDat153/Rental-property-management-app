## Context

The backend already contains JPA models for invoices, invoice items, maintenance requests, request images, contracts, rooms, users, services, and meter readings, but there are no invoice or maintenance controllers/application services exposed to Android. The Android app already uses Retrofit, DTOs, XML/View-based fragments, Vietnamese feedback, and navigation destinations for related rental resources.

## Goals / Non-Goals

**Goals:**

- Add resource-oriented CRUD boundaries for invoices and maintenance requests using DTOs and relationship validation.
- Keep invoice line items nested in invoice create/update payloads and responses.
- Connect both resources to Android list/detail/form workflows with explicit loading, empty, error, validation, and confirmation states.
- Keep meter management attached to room detail while utilities navigation exposes invoices, maintenance, and service catalog.

**Non-Goals:**

- Automatic invoice calculation from meters or services.
- Payment gateway integration, notifications, or scheduled invoice generation.
- New database tables or changes to existing column meanings.
- Replacing existing contract, room, user, service, or meter screens.

## Decisions

### Backend resource boundaries

Create separate controllers and application services for invoices and maintenance. Invoice endpoints use contract-scoped filtering and accept line-item references by service and optional meter reading IDs. Maintenance endpoints accept room/user IDs and expose request images as URL summaries rather than nested persistence entities.

Alternative considered: expose JPA entities directly. Rejected because it risks recursive contract/room graphs and couples Android payloads to persistence mappings.

### Invoice totals

The API accepts explicit subtotal, discount, tax, and total values for this change. It validates non-negative monetary values and item amounts but does not calculate totals automatically, matching the existing database model and avoiding an unrequested billing formula.

Alternative considered: calculate totals server-side from services and readings. Deferred because the current model does not define a single authoritative tariff/calculation rule.

### Maintenance lifecycle

Use the existing `PENDING`, `PROCESSING`, and `COMPLETED` statuses and existing priority string field. When a request becomes completed, the service sets completion metadata; when it leaves completed state, completion metadata is cleared or handled consistently by the service contract.

### Android navigation and UI

Add invoice and maintenance destinations and replace only the Meter action in `ServiceFragment`. Meter navigation remains in `RoomDetailFragment`. Each new screen follows the existing user/meter pattern: a simple header, white content cards, native controls, Vietnamese states, and dialogs or detail forms for mutations.

### Deletion safety

Destructive operations require Android confirmation. Backend delete operations validate dependent relationships and return stable conflict responses instead of partially deleting records.

## Risks / Trade-offs

- [Risk] Invoice totals can be inconsistent with line items because this change accepts explicit totals -> validate non-negative values and display the submitted totals; add calculation rules in a later change when billing semantics are defined.
- [Risk] Existing databases may contain incomplete legacy invoice or maintenance references -> return relationship validation errors and avoid exposing null-required fields as valid records.
- [Risk] Maintenance image upload behavior is not yet represented by an Android contract -> initially support existing image URL summaries and add multipart upload only if the existing request-image uploader contract is confirmed during implementation.
- [Risk] Large invoice item lists may make a dialog difficult to use -> use a scrollable detail/form layout and keep the list summary compact.

## Migration Plan

1. Deploy backend endpoints and DTOs without schema migration.
2. Add Android API models, navigation, screens, and mutation flows.
3. Verify existing contracts, rooms, services, meters, and tenant workflows remain unchanged.
4. Roll back by disabling the new utilities entry points and endpoints; existing resource data remains intact.
