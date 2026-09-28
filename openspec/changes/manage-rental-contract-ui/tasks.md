## 1. Contract Data And API

- [x] 1.1 Add Android RentalContract and RentalContractRequest models matching the existing contract response/request fields, and verify Gson maps UUID, date, decimal, enum, nullable, and document values correctly.
- [x] 1.2 Add Retrofit methods for contract list/detail/multipart create/multipart update/delete and verify the generated requests use `/be/contracts`, the `contract` part, and the optional `document` part names.
- [x] 1.3 Add client-side contract-room display mapping and case-insensitive room-code filtering, and verify missing rooms render `Chưa xác định phòng` and do not match non-empty room-code queries.

## 2. Contracts List And Navigation

- [x] 2.1 Replace the placeholder contracts destination with a dedicated list fragment and XML screen using the UI guide's palette, card surfaces, spacing, labels, and minimum touch targets; verify the Android resource compilation succeeds.
- [x] 2.2 Add a contract list row showing room code, period, rent, and text status, and verify populated, empty, loading, unresolved-room, and request-error states are distinguishable in Vietnamese.
- [x] 2.3 Load contracts and rooms, coordinate their states, connect the room-code filter, and navigate a row to the contract detail destination; verify the list does not show stale rows while required data is loading.
- [x] 2.4 Register the contract detail destination and string identifier argument in Navigation Component configuration, and verify row navigation and back navigation resolve the correct contract.

## 3. Create And Edit Form

- [x] 3.1 Add the scrollable XML form with labeled room, date, financial, payment, status, terms, and document sections following the Room form pattern; verify small-width and large-font layouts do not clip required fields or actions.
- [x] 3.2 Implement room loading and selection using existing rooms, date controls, status labels, termination date/reason fields, and field-level validation for required values, non-negative amounts, payment ranges, and end-date ordering; verify invalid submission preserves valid inputs and does not issue a request.
- [x] 3.3 Implement single-PDF/DOCX selection with `ACTION_OPEN_DOCUMENT`, MIME/extension/readability validation, selected-file feedback, and ContentResolver streaming; verify unsupported and unreadable URIs are rejected before submission.
- [x] 3.4 Serialize the request JSON and optional PDF into the existing multipart contract API for create and update, guard duplicate submissions, and verify no document part is sent when editing without a replacement file.
- [x] 3.5 Handle create/update success, validation, upload, not-found, conflict, connectivity, and unavailable-room feedback in Vietnamese; verify the returned server state is shown after success.

## 4. Detail And Document Actions

- [x] 4.1 Add the scrollable contract detail XML with summary, grouped contract information, read-only lifecycle metadata, document section, update action, and separated delete action; verify missing fields display `Chưa cập nhật` and actions remain reachable.
- [x] 4.2 Load and bind contract detail with resolved room code, allow editing contract fields including termination date and reason, and refresh displayed values after a successful update; verify an unresolved room remains identifiable without exposing its UUID as the primary label.
- [x] 4.3 Add confirmed contract deletion and map dependent-record HTTP 409 responses to recoverable Vietnamese conflict feedback; verify successful deletion returns to the list without showing stale detail data.
- [x] 4.4 Implement document viewing through Android intent resolution with PDF/browser fallback and actionable failure feedback; verify view actions are unavailable when `document_url` is blank or no handler exists.
- [x] 4.5 Implement native document download with `DownloadManager`, notification-visible progress, and a stable filename; verify a valid document URL starts a download without blocking the detail screen.
- [x] 4.6 Persist editable signed date, termination date, and termination reason from the detail update request, and allow optional PDF/DOCX replacement upload; verify omission preserves the current document and selected replacement updates its URL.

## 5. Verification

- [x] 5.1 Add focused tests for room-code joining/filtering, date and amount validation, request serialization, and missing-document behavior; verify the tests pass.
- [x] 5.2 Build the Android module with the repository's Gradle command and verify all new Java, XML, navigation, and resource files compile.
- [ ] 5.3 Manually verify populated, empty, loading, API-error, unresolved-room, invalid-form, upload-failure, missing-document, external-view failure, download, update, and delete-conflict flows on a small and larger Android viewport.
