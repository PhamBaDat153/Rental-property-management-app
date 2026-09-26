## Context

The Android app is a Java/XML Retrofit application. `PropertiesFragment` currently owns list loading, filtering, dialogs, and resource mutations. Location currently stores only address names (`province_name`, `district_name`, `ward_name`) and Room create/update already use multipart endpoints, while the backend uploads accepted images to Cloudinary and returns `image_urls`. The existing User create dialog provides the visual baseline for labeled forms, but it is too small for the Room and Location fields now required.

## Goals / Non-Goals

**Goals:**

- Use provinces.open-api.vn v2 to provide dependent Vietnamese address selectors without changing persistence fields.
- Replace dynamic resource form construction with maintainable XML layouts and visible labels.
- Select up to six Room images, preview/remove them before submit, and encode them as multipart parts.
- Preserve existing Room images when an edit contains no new image parts.
- Move Location and Room inspection/mutation into dedicated Navigation destinations.
- Keep the existing resource list filtering and API error behavior consistent.

**Non-Goals:**

- No province/district/ward code columns or database migration.
- No backend change to delete or replace individual old Room images.
- No image editing, cropping, compression service, offline province cache, or pagination.
- No change to the existing Location and Room REST resource semantics.

## Decisions

### Use province API v2 with dependent loading

The client will use the v2 API. It will load the province list first, then request the selected province's districts and the selected district's wards. Only `name` values are submitted to the existing Location request. This avoids the large all-level `depth=3` response and matches the current database model.

The alternative is one `depth=3` request and in-memory traversal. It reduces request count but downloads a large payload and is explicitly discouraged by the provider documentation.

### Use XML-backed, scrollable forms

Create/edit forms will be dedicated XML layouts inside `ScrollView`, following the User form's visible label, 48dp control, card background, and Vietnamese validation pattern. Fragments will bind fields and submit typed request models.

The alternative is continuing to construct fields in Java. That is smaller initially but makes the Room image grid, dependent selectors, field-level errors, and small-screen layout harder to maintain.

### Add two detail destinations

`LocationDetailFragment` and `RoomDetailFragment` will receive a string UUID navigation argument, load the current record by ID, and own detail rendering, edit navigation, and confirmed deletion. `PropertiesFragment` will remain responsible for list/filter state and will navigate to these destinations rather than opening detail dialogs.

The alternative is one generic detail fragment, but Location and Room have different field groups and Room has an image gallery; separate fragments keep each screen readable and avoid conditional layout sprawl.

### Use Android document picker and URI multipart parts

Room forms will use `ACTION_OPEN_DOCUMENT` with `image/*` and multiple selection. The selected URIs will be capped at six, previewed in a simple horizontal/vertical container, and converted to `MultipartBody.Part` values through a content resolver when saving. No selected images means an empty image-part list, so the backend leaves existing images unchanged during update.

The alternative is a third-party image picker, but the platform picker covers selection without a new dependency. Image removal of already-uploaded URLs is excluded because the backend has no delete-image endpoint.

### Avoid extra backend fields

Province, district, and ward codes remain client-only selection metadata. The request continues sending names into the existing fields. This keeps compatibility with current API and database consumers.

## Risks / Trade-offs

- [Risk] The province service can be unavailable or change its v2 payload. -> Mitigation: model the documented response, show a recoverable error, and block submission when a required province is not selected.
- [Risk] Content URIs may not expose a filesystem path or may be revoked. -> Mitigation: stream through `ContentResolver`, retain URI permissions where supported, and reject unreadable items before submit.
- [Risk] Six full-resolution images can create large multipart requests. -> Mitigation: cap selection at six and keep image compression/resizing as a later capability rather than silently altering user files.
- [Risk] Existing Room images cannot be removed individually. -> Mitigation: preserve all existing URLs when no new images are selected and label image replacement/removal as out of scope.
- [Risk] Moving detail handling from dialogs changes back-stack behavior. -> Mitigation: pass stable UUID arguments, use `navigateUp`, and reload the list on return/success.

## Migration Plan

1. Add province API models/service and dependent selector loading.
2. Add XML Location and Room forms and migrate create/edit entry points.
3. Add URI image selection, six-image validation, previews, and multipart encoding.
4. Add both detail fragments and navigation arguments, then remove resource detail dialogs.
5. Run unit/build checks and manually verify API, picker, back navigation, and conflict flows.
6. Roll back by restoring the prior `PropertiesFragment` dialog/detail behavior; no server or database rollback is required.

## Open Questions

None. The API version, stored values, six-image cap, existing-image preservation rule, and two-detail-fragment scope are confirmed decisions.
