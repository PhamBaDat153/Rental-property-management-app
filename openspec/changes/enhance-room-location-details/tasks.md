## 1. Province API And Location Forms

- [x] 1.1 Add typed province, district, and ward models plus Retrofit methods for the provinces.open-api.vn v2 endpoints; verify the Android module compiles and the response models map documented `name`, `code`, and child fields.
- [x] 1.2 Implement dependent province, district, and ward loading with reset behavior, loading states, API failure feedback, and name-only request mapping; verify selecting a parent replaces invalid lower-level selections.
- [x] 1.3 Create XML-backed Location create/edit form following the User form pattern, including visible labels, required validation, status selection, address line, description, and save-state protection; verify invalid submissions preserve entered values and send no request.
- [x] 1.4 Migrate Location create/edit entry points to the XML form and structured selectors; verify successful create/update refreshes the list and persists selected province/district/ward names.

## 2. Room Form And Image Selection

- [x] 2.1 Create XML-backed Room create/edit form with Location selector, room fields, numeric validation, status, description, image preview area, and labeled controls; verify the form scrolls without clipping on a small viewport.
- [x] 2.2 Add document-picker image selection with `image/*`, multi-select, readable URI validation, preview/remove behavior, and a maximum of six selected images; verify a seventh selection is rejected or truncated with feedback.
- [x] 2.3 Convert selected content URIs to multipart image parts and submit Room JSON plus images through the existing multipart API; verify the request includes zero to six image parts and rejects unreadable/non-image content.
- [x] 2.4 Preserve existing Room images when editing without new image selection and display returned image URLs after successful mutation; verify no replacement image parts are sent for an unchanged image set.

## 3. Dedicated Detail Fragments And Navigation

- [x] 3.1 Add `LocationDetailFragment` and its XML layout with UUID argument, loading/error/empty states, grouped Location information, edit action, and confirmed delete action; verify row navigation loads the requested Location and back navigation returns to the list.
- [x] 3.2 Add `RoomDetailFragment` and its XML layout with UUID argument, grouped Room information, Location context, image gallery/empty state, edit action, and confirmed delete action; verify each returned image has a meaningful content description.
- [x] 3.3 Add Navigation Component destinations and arguments, then change `PropertiesFragment` row actions to navigate instead of opening detail dialogs; verify Location and Room rows reach the correct fragment with the correct identifier.
- [x] 3.4 Return from successful edits/deletes with refreshed list/detail state and preserve recoverable context on API conflicts or failures; verify referenced Location/Room deletion conflicts do not remove stale records optimistically.

## 4. Cleanup And Verification

- [x] 4.1 Remove or bypass obsolete resource detail/form dialog paths while preserving existing list filters and Vietnamese resource states; verify no duplicate create/edit/detail entry point remains.
- [x] 4.2 Add focused tests for province-to-name mapping, selector reset behavior, six-image cap, no-new-image preservation, and Room/Location navigation arguments; verify unit tests pass.
- [x] 4.3 Run the Android build and unit test commands from `Source/Rentaly_Management` and fix resource, Retrofit, navigation, or multipart errors.
- [ ] 4.4 Manually verify province loading, dependent selectors, Location CRUD, Room CRUD, image selection/upload, image empty state, detail navigation, delete conflicts, and small-screen accessibility against the running backend.
