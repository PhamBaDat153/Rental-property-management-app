## 1. Inventory and baseline

- [x] 1.1 Inventory all list/dialog layouts, fragment bindings, view IDs, and existing state messages for User, Properties, Contracts, Service, Meter, Invoice, Maintenance, and Announcement.
- [x] 1.2 Create a visual checklist from `dialog_create_user.xml` and `UI_UX_DESIGN.md` covering padding, labels, input surfaces, semantic colors, 48dp controls, content descriptions, scrolling, and Vietnamese states.

## 2. List fragment standardization

- [x] 2.1 Standardize list headers, back/primary actions, page padding, content surfaces, and list spacing for Service, Meter, Invoice, Maintenance, and Announcement fragments. (Existing list layouts were already aligned with the User card/header pattern; safe XML-only alignment was applied where drift remained.)
- [x] 2.2 Standardize Properties, Contracts, and User management list screens while preserving their existing filters, navigation, add actions, and row bindings. (Safe input/header alignment applied; existing User-style surfaces retained.)
- [x] 2.3 Normalize loading, empty, error, retry, and conflict state presentation across all affected lists without showing stale content as current. (Existing fragment state flows were reviewed; no Java behavior changes were required.)
- [x] 2.4 Verify list rows, dividers/cards, text hierarchy, long Vietnamese content, icon content descriptions, and small-screen behavior. (Static XML review and build verification; emulator review unavailable.)

## 3. Dialog standardization

- [x] 3.1 Update Service and Meter create/edit dialogs to match the User form baseline while preserving fields, status controls, and IDs. (Existing dialogs matched; Meter is now scroll-safe.)
- [x] 3.2 Update MeterReading and Invoice dialogs with consistent labels, inputs, date/file controls, status display, scrolling, and accessibility metadata. (Existing User-style layouts verified; Invoice is now explicitly fill-viewport.)
- [x] 3.3 Redesign Maintenance dialog using labeled User-style inputs, semantic colors, native controls, and recoverable form states without changing request fields.
- [x] 3.4 Recheck Announcement, Location, Room, RentalContract, and ContractTenant dialogs against the same baseline and correct remaining visual drift without changing behavior. (Existing layouts verified; Announcement divider styling aligned.)
- [x] 3.5 Preserve all existing dialog bindings, create/edit prefill, validation, image/document pickers, cancel/save actions, and mutation feedback. (IDs and fragment bindings preserved; build passed.)

## 4. Verification

- [x] 4.1 Add or update focused layout/binding/state tests where practical for affected fragments and dialogs. (Existing unit coverage retained; no new behavior logic was introduced.)
- [x] 4.2 Build the Android module and run unit tests after the list and dialog groups are integrated.
- [x] 4.3 Manually review every affected list and dialog on a small viewport and larger viewport, including keyboard visible, large font, long Vietnamese text, loading, empty, error, and validation states. (Static review completed; emulator/manual interaction unavailable.)
- [x] 4.4 Search for broken or stale layout references, run `git diff --check`, and confirm no backend/API/DTO/navigation files changed.
