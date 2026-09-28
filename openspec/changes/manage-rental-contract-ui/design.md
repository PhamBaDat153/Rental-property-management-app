## Context

The Android app is a Java/XML Retrofit application. `contractsFragment` currently points to the generic `ScreenFragment`, while resource management already provides list, form, and dedicated detail patterns for locations and rooms. The backend exposes `/be/contracts` as JSON list/detail plus multipart create/update and delete endpoints, and `/be/rooms` already exposes the room identifiers and codes needed for client-side context. The response contract contains `room_id` but not `room_code`, so the Android screen must resolve that relationship locally.

The existing app uses `ApiService`, simple DTO classes, XML layouts, Navigation Component destinations, `ACTION_OPEN_DOCUMENT` for platform file selection, and direct Retrofit callbacks. The visual and interaction constraints are defined in `UI_UX_DESIGN.md`: gray page surface, white rounded content cards, labeled controls, scrollable forms/details, explicit data states, Vietnamese feedback, and confirmed destructive actions.

## Goals / Non-Goals

**Goals:**

- Add one coherent list-to-form-to-detail contract workflow under the existing Android navigation.
- Resolve room codes on the client by joining loaded contract and room data through UUIDs.
- Keep contract create/update compatible with the existing multipart request names: `contract` and optional `document`.
- Restrict Android document selection to one PDF or DOCX and retain the existing document when an edit does not select a replacement.
- Use platform document handoff and download behavior without introducing a PDF rendering dependency.
- Make loading, empty, unresolved-room, validation, conflict, upload, and external-intent failures visible and recoverable.

**Non-Goals:**

- No backend DTO, database, endpoint, or response-shape changes.
- No management of contract tenants or invoices from this screen; their existence only affects delete conflict feedback.
- No embedded PDF viewer, PDF annotation, file editing, document replacement history, or document deletion endpoint.
- No server-side search, pagination, offline contract cache, or room-code filtering API.
- No change to the existing Room/Location workflows beyond sharing their room data and navigation conventions.

## Decisions

### Join contracts to rooms in Android

The contracts screen will load `/be/contracts` and `/be/rooms`, index rooms by `room_id`, and derive a display model containing `room_code`. Filtering uses only the derived room code and is case-insensitive. A missing room is rendered as `Chưa xác định phòng`, while the raw UUID is not used as the primary user-facing label.

Changing `RentalContractResponse` to include room fields was considered, but would expand the API contract for a UI need that the existing room endpoint already satisfies. Client joining also keeps the decision aligned with the confirmed scope and avoids a backend change.

### Use dedicated XML surfaces and fragments

The placeholder contracts destination will become a dedicated list fragment. Separate XML form and detail layouts will follow the existing `form_room.xml` and `fragment_room_detail.xml` patterns, but will use contract-specific field groups: room, dates, finances/payment, management/terms, and document. The form and detail content will be vertically scrollable with a reachable final action area; list rows remain compact and expose only decision-relevant values.

A separate detail fragment is preferred over dialogs because contract data includes multiple grouped fields and document actions. A shared generic contract/room detail layout was considered, but would create conditional views and weaken the established domain-specific hierarchy.

### Preserve the existing multipart API contract

The Android request DTO will mirror the backend request fields and be serialized as JSON in a `RequestBody` part named `contract`. The selected PDF will be streamed from its `content://` URI through `ContentResolver` into a multipart part named `document`. No selected file means no document part, allowing the backend's existing update behavior to preserve the current URL.

The platform `ACTION_OPEN_DOCUMENT` picker will accept PDF and DOCX MIME types, allow a single selection, and validate the content type, extension, and readability before submission. The implementation will avoid filesystem-path assumptions because document-provider URIs are not guaranteed to expose paths.

### Delegate viewing and downloading to Android

The detail screen will use `Intent.ACTION_VIEW` with the document URL and a PDF MIME type. If no handler exists, it will retry as a browser-compatible view where appropriate and then show actionable Vietnamese feedback. Downloads will use `DownloadManager` with a visible notification and a stable filename derived from the URL or a contract fallback. Both actions are enabled only when a non-blank document URL exists.

An embedded PDF viewer was considered but rejected because it adds a dependency, increases binary and maintenance cost, and conflicts with the confirmed native-device behavior.

### Treat status and lifecycle fields according to backend semantics

The form will expose the supported `ACTIVE`/`INACTIVE` status labels in Vietnamese for editing. The backend currently defaults newly persisted contracts to `INACTIVE` during `prePersist`; the create flow must not imply that selecting `ACTIVE` necessarily overrides that server behavior. The detail screen allows editing `signed_at`, `terminated_at`, and `termination_reason`; the backend request and update mapping persist those values. A replacement PDF/DOCX may be selected from detail and is sent as the optional multipart `document` part; omitting it preserves the existing URL.

### Model request states explicitly

List loading will coordinate the contract and room calls so stale rows are not presented as current. If the room request fails, the UI will distinguish that related-data failure and offer retry; existing contracts may be shown with an unavailable-room label only when contract data itself is valid. Submit buttons will be guarded during requests. Delete uses a specific confirmation dialog and maps HTTP 409 to dependent-record conflict feedback without navigating away as though deletion succeeded.

## Risks / Trade-offs

- [Risk] The two list requests can finish at different times or one can fail. -> Mitigation: track each request state, render only a coherent loaded state, provide retry, and use an explicit unavailable-room representation when only relationship data is missing.
- [Risk] Room list changes can make a previously valid contract unresolved. -> Mitigation: preserve the contract row and label its room context as unavailable instead of hiding the record or exposing a UUID as its name.
- [Risk] Cloudinary URLs or installed apps may not support direct PDF viewing. -> Mitigation: use Android intent resolution, offer browser-compatible fallback, and keep download as a separate action with actionable failure feedback.
- [Risk] Content URIs can be unreadable or have missing MIME metadata. -> Mitigation: inspect MIME and filename, stream through `ContentResolver`, reject invalid/unreadable files before submission, and preserve entered form values.
- [Risk] The backend forces new contract status to `INACTIVE`. -> Mitigation: show the returned server state after create and document the behavior in implementation tests rather than presenting optimistic client state.
- [Risk] Contract deletion can be blocked by tenant or invoice dependencies. -> Mitigation: confirm before delete and map the conflict response to a stable Vietnamese message while keeping the detail screen available.

## Migration Plan

1. Add Android contract DTO/request classes and Retrofit methods for list/detail/multipart create/update/delete.
2. Add the contracts list fragment, row layout, filter, room join, and navigation destination.
3. Add XML create/edit form, PDF picker/URI handling, validation, multipart serialization, and request-state feedback.
4. Add the contract detail layout/fragment, edit flow, delete confirmation/conflict handling, document view handoff, and native download action.
5. Replace the placeholder contracts navigation destination and verify populated, empty, loading, unresolved-room, validation, conflict, missing-document, and external-intent states.
6. Build the Android module and run focused tests for room-code filtering, request serialization, validation, and date/amount handling.
7. Roll back by restoring the placeholder destination and removing only the new contract Android files; no server or database rollback is required.

## Open Questions

None. The remaining implementation choices are covered by the confirmed scope and existing API/UI constraints.
