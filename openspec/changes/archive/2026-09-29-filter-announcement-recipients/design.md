## Context

The Android `AnnouncementFragment` currently loads `UserTenant` profiles and passes their IDs to the existing announcement request. The backend announcement API accepts only `recipient_ids` and has no room filter. Room membership is available indirectly through `Room`, `RentalContract`, and `ContractTenant` responses. The announcement dialog already uses an XML layout and a multi-choice `ListView`.

## Goals / Non-Goals

**Goals:**

- Enrich the recipient list locally from existing API responses.
- Deduplicate by tenant/user identity and aggregate all related room labels.
- Provide name search, room selection, visible-row bulk selection, and Vietnamese loading/error/empty feedback.
- Keep existing create/edit request payloads and the User-style dialog visual language.

**Non-Goals:**

- No backend endpoint, response DTO, database, or `AnnouncementRequest` changes.
- No filtering of historical announcements after they are created.
- No new tenant or room search service.
- No change to announcement delivery, draft/send state, or recipient permissions.

## Decisions

### Build a local recipient view model

Keep the existing `UserTenant` data as the source of identity, and derive a small local display model containing the tenant ID, display name, and a deduplicated set of room labels. This avoids mutating DTOs and makes filtering independent of raw API object structure.

### Join room membership through contracts

Load rooms, contracts, and contract-tenant assignments, then map `contract_id -> room_id` and `tenant_id -> room labels`. This matches the current domain relationships without adding an endpoint. A direct backend filter was rejected because the user explicitly chose Android-only behavior.

### Aggregate multiple rooms into one row

A tenant appears once because the request accepts user IDs and duplicate visual rows would create ambiguous selection. Room labels are joined into one readable summary such as `Phòng P101, P202`. When a tenant has no resolved room, keep the tenant selectable and show `Chưa xác định phòng`.

### Filter the displayed list, not the selection source

Maintain a complete recipient collection and a separate filtered projection. Selection state is keyed by unique tenant/user ID, so changing filters does not lose selections and select-all affects only visible rows.

### Keep room filtering native and simple

Use a native Spinner for room selection and an EditText for name search, matching existing Android controls and avoiding a new dependency or custom picker. The room list includes an `Tất cả phòng` option.

## Risks / Trade-offs

- [Risk] Three related requests increase loading time and partial-failure cases. -> Mitigation: load them before opening the form, show progress/error feedback, and retain a recoverable retry path.
- [Risk] A tenant may have stale or overlapping historical contracts. -> Mitigation: aggregate all resolved rooms as explicitly decided; a future active-contract rule can be added as a separate requirement.
- [Risk] A visible-row select-all action can surprise users when filters change. -> Mitigation: label it `Chọn tất cả đang hiển thị` and preserve selections outside the current filter.
- [Risk] Large tenant lists may make a fixed-height ListView hard to scan. -> Mitigation: retain the scrollable dialog and use the existing list control; optimize only if measured data size requires it.

## Migration Plan

1. Add the local recipient display model and related-data loading coordination in the Android announcement flow.
2. Add dialog controls for name search, room selection, visible select-all, and filtered empty/loading/error states.
3. Keep the existing announcement create/update request construction and deduplicate submitted IDs.
4. Build the Android module and verify filtering, aggregation, selection persistence, and failure recovery with representative data.
5. Roll back by removing the local enrichment/filter controls and restoring the current UserTenant-only recipient list; no backend rollback is required.
