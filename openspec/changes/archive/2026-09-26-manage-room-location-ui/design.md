## Context

The Android app is a Java/XML application using Retrofit and Navigation Component. `propertiesFragment` currently points to the generic `ScreenFragment`, while User management already establishes the local list, detail, dialog, error, and destructive-confirmation patterns. The backend exposes DTO-shaped Location endpoints and Room endpoints whose create/update operations use multipart form data. The database model relates rooms to locations and protects referenced resources from deletion.

## Goals / Non-Goals

**Goals:**

- Make `propertiesFragment` a usable entry point for both Location and Room management.
- Reuse existing colors, cards, spacing, native controls, Retrofit conventions, and Navigation patterns.
- Keep the UI model aligned with backend DTO fields and relationship constraints.
- Keep list, detail, create, edit, delete, filter, loading, empty, and error behavior explicit.

**Non-Goals:**

- No backend controller, service, database, or API-contract redesign.
- No separate Location name field or UI label that implies one exists.
- No room-name fallback to `room_code`.
- No pagination, offline cache, advanced sorting, or image gallery workflow in this change.

## Decisions

### Use one properties fragment with two resource views

The properties destination will host Location and Room views under one screen, using a simple native two-option selector such as tabs or segmented controls. This keeps the bottom-navigation destination stable and avoids duplicating a second top-level destination. Separate detail destinations may be used for cleaner back-stack behavior.

An alternative was one combined interleaved list, but that would mix two different filters and create ambiguous add/detail actions. Separate resource views keep the user's mental model and API calls clear.

### Use client-side filtering after loading the existing list

Location and Room list endpoints currently expose collection reads without search parameters. The Android client will load the list and filter the in-memory records locally: Location queries inspect code and address components; Room queries inspect only `room_name`.

An alternative was adding query parameters and backend filtering, but that expands the approved scope and changes existing APIs without a demonstrated scale requirement.

### Keep DTOs separate from form requests

The Android layer will use response models for displayed records and request models for create/update payloads. Room requests will be encoded as the JSON `room` multipart part plus optional image parts, matching the backend contract even if image selection remains out of scope for the first UI.

An alternative was posting generic maps, but typed models make enum, numeric, relationship, and nullable fields easier to validate and keep aligned with the backend records.

### Use detail screens for inspection and mutation

Rows open detail views. Detail views show grouped information first and place edit and destructive actions after the record content. Create/edit forms use visible labels, validation near the relevant field, and a final save action.

An alternative was keeping all editing in list dialogs, but the Room and Location forms have enough fields that dialogs would become cramped on small Android screens and diverge from the UI guide.

### Preserve explicit display semantics

Location identity is rendered from `location_code` and address fields. A blank Room `room_name` renders as `Chưa đặt tên phòng`; `room_code` is rendered separately as metadata. Status includes a text label and is not communicated by color alone.

### Map common API failures to recoverable feedback

Connectivity failures, malformed responses, validation errors, duplicate codes, not-found responses, and deletion conflicts will produce Vietnamese feedback without leaving the user on a broken or blank screen. Delete actions require confirmation before the request is sent.

## Risks / Trade-offs

- [Risk] Client-side filtering downloads the complete resource list and may become slow for a large portfolio. -> Mitigation: keep the initial contract simple; introduce server-side search/pagination as a separate change when list size justifies it.
- [Risk] Room multipart encoding is more complex than the existing JSON-only Retrofit calls. -> Mitigation: model the JSON part explicitly and verify create/update requests against the backend's expected `room` part and optional `images` parts.
- [Risk] Location deletion can fail because rooms reference it, and Room deletion can fail because contracts reference it. -> Mitigation: preserve the list/detail state and display the backend conflict message instead of optimistically removing the row.
- [Risk] The current project has limited Android UI test infrastructure. -> Mitigation: add focused model/filter tests where practical and verify the build plus manual navigation and API error paths during implementation.
- [Risk] Room list responses provide only `location_id`, not a nested Location response. -> Mitigation: display the identifier initially or resolve a local Location label from the already loaded Location list; do not invent a backend join response.

## Migration Plan

1. Add Android resource DTOs, request models, Retrofit declarations, adapters, layouts, fragments, and navigation destinations.
2. Replace the `propertiesFragment` class mapping while keeping its existing bottom-navigation ID.
3. Build the Android module and manually verify list, filter, detail, create, edit, delete confirmation, and conflict feedback against the existing backend.
4. Roll back by restoring the `propertiesFragment` mapping to `ScreenFragment`; no database or backend rollback is required.

## Open Questions

None that change the approved behavior. The implementation may choose tabs versus a segmented native selector and may decide whether to reuse one generic detail form or two resource-specific detail fragments, provided the specified observable behavior remains unchanged.
