## Why

The current Location form accepts province, district, and ward as free text, which invites inconsistent Vietnamese address values. Room creation also has no image picker, and Room/Location details are shown in dialogs rather than dedicated screens, making multi-field review and editing uncomfortable on small Android devices.

## What Changes

- Use `https://provinces.open-api.vn/api/v2/` to provide dependent province, district, and ward selectors in the Location form.
- Persist only the selected location names in the existing `province_name`, `district_name`, and `ward_name` fields; do not change the database schema.
- Replace ad-hoc resource forms with XML-backed create/edit layouts following the existing User form visual pattern.
- Add Room image selection with a maximum of six images per room and multipart upload support.
- Preserve existing Room images when an edit submits no new images.
- Add `LocationDetailFragment` and `RoomDetailFragment` for dedicated detail, edit, and delete workflows.
- Display Room image URLs in the detail screen and keep destructive actions confirmed.

## Capabilities

### New Capabilities

- `rental-resource-detail-ui`: Dedicated Location and Room detail screens, resource forms, province selectors, and Room image management.

### Modified Capabilities

- `rental-resource-ui`: Extend the existing resource-management behavior with structured address selection, Room image creation, and dedicated detail navigation.

## Impact

- Android fragments, layouts, DTOs, Retrofit services, Navigation Component routes, URI/image multipart handling, and local tests under `Source/Rentaly_Management/app`.
- Read-only integration with `provinces.open-api.vn` v2.
- Existing Room multipart API and Cloudinary upload flow; no database schema change.
- Existing main spec `openspec/specs/rental-resource-ui/spec.md` plus a new detail UI capability spec during archive sync.
