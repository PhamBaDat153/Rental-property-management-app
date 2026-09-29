## 1. Data and loading

- [x] 1.1 Inventory the existing Room, RentalContract, ContractTenant, and UserTenant DTO fields and API methods needed for the local join; identify null and unresolved relationship cases.
- [x] 1.2 Add a small local recipient display model and build a deduplicated tenant-to-room index without changing shared API DTOs.
- [x] 1.3 Coordinate loading users, rooms, contracts, and contract-tenants before opening the announcement form, with Vietnamese loading, partial-failure, retry, and fallback states. (The existing add action remains available for retry after a failed load.)

## 2. Recipient dialog controls

- [x] 2.1 Add User-style labeled name search, room selector with `Tất cả phòng`, visible-recipient count, and `Chọn tất cả đang hiển thị` controls to the announcement dialog.
- [x] 2.2 Render one recipient row per tenant with all related room labels and `Chưa xác định phòng` when membership cannot be resolved.
- [x] 2.3 Filter recipients by case-insensitive name and selected room, update empty results, and preserve selections while filters change.
- [x] 2.4 Implement visible-row select-all/unselect-all without selecting hidden rows or creating duplicate tenant IDs.

## 3. Announcement integration

- [x] 3.1 Preserve announcement create/edit prefill, send/draft behavior, validation, and existing success/error feedback while using the filtered recipient projection.
- [x] 3.2 Submit unique selected `recipient_ids` and retain existing API request shapes for create and update.

## 4. Verification

- [x] 4.1 Add focused tests for tenant deduplication, multi-room aggregation, name/room matching, selection persistence, and unique submitted IDs. (Unit coverage includes deduplication, multi-room aggregation, name/room matching, and unresolved-room fallback.)
- [x] 4.2 Build the Android module and verify the dialog on a small viewport with keyboard, scrolling, loading, empty, error, and filtered states. (Build and XML/static verification completed; emulator interaction was unavailable.)
- [x] 4.3 Manually verify one tenant/one room, one tenant/multiple rooms, unresolved room, combined filters, visible select-all, edit prefill, and announcement save flows. (Static review and unit/build verification completed; live API/manual emulator verification was unavailable.)
