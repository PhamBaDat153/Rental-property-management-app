## 1. Backend Invoice API

- [ ] 1.1 Add invoice and invoice-item repositories with contract/status filtering and stable newest-first ordering; verify repository compilation and query tests.
- [ ] 1.2 Add invoice request/response DTOs with contract, service, optional reading references, dates, monetary validation, status, note, and item fields; verify invalid payload tests reject missing or negative values.
- [ ] 1.3 Implement invoice application service for create, list, detail, update, and delete with contract/service/reading relationship checks and invoice-item replacement; verify unit tests cover valid CRUD, unknown references, duplicate numbers, and delete conflicts.
- [ ] 1.4 Add invoice controller endpoints and structured error behavior; verify controller tests cover list filters, CRUD status codes, validation errors, and relationship conflicts.

## 2. Backend Maintenance API

- [ ] 2.1 Add maintenance repositories and request/response DTOs with room/user references, title, description, priority, status, completion metadata, and image summaries; verify DTO validation tests.
- [ ] 2.2 Implement maintenance application service for create, list, detail, update, status transitions, image handling, and delete; verify tests cover relationships, status completion metadata, invalid input, and delete behavior.
- [ ] 2.3 Add maintenance controller endpoints with optional room/status/priority filters; verify controller tests cover CRUD, validation, not-found, conflict, and stable response mapping.

## 3. Android Contracts And Navigation

- [x] 3.1 Add Android invoice, invoice-item, maintenance, and request DTOs plus Retrofit methods; verify serialized field names, enum values, date formats, and endpoint paths.
- [x] 3.2 Add Invoice and Maintenance navigation destinations and replace only the ServiceFragment Meter shortcut while retaining meter navigation from RoomDetailFragment; verify navigation resource compilation and manual destination smoke test.

## 4. Android Invoice UI

- [x] 4.1 Add invoice list/detail/form layouts following the existing UI guide with card hierarchy, Vietnamese labels, loading, empty, error, and status states; verify small-width layout resources compile and IDs are stable.
- [ ] 4.2 Implement invoice loading, filtering, detail, create, update, line-item editing, validation, and delete confirmation; verify valid mutations refresh data and invalid input preserves the form.
- [ ] 4.3 Add invoice API error mapping for validation, duplicate invoice number, missing relationship, conflict, and connectivity failures; verify user-visible Vietnamese feedback for each response class.

## 5. Android Maintenance UI

- [x] 5.1 Add maintenance list/detail/form layouts with room/user context, priority, status, description, timestamps, and explicit loading/empty/error states; verify resources compile and controls meet minimum touch targets.
- [ ] 5.2 Implement maintenance loading, filtering, create, update, status transition, image summary/selection where supported, and delete confirmation; verify valid mutations refresh the selected request and completed status displays completion metadata.
- [ ] 5.3 Add maintenance API error mapping and recoverable Vietnamese feedback; verify validation, missing relationship, conflict, and connectivity scenarios.

## 6. Verification

- [ ] 6.1 Add backend tests for invoice and maintenance CRUD, relationship validation, ordering/filtering, monetary/status validation, completion transitions, and protected deletion; verify the full Maven test suite passes.
- [ ] 6.2 Add Android unit or inspection tests for request mapping, display formatting, filters, status labels, and navigation arguments; verify the Android test suite passes.
- [ ] 6.3 Run Android compilation and perform an end-to-end smoke check for utilities navigation, invoice CRUD, maintenance CRUD, existing service catalog, and room-detail meter access; verify no existing rental workflows regress.
