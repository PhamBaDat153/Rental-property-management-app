## 1. Baseline and layout preparation

- [x] 1.1 Inventory all view IDs and form bindings used by `PropertiesFragment`, `ContractsFragment`, and `ContractDetailFragment`; record the current create/edit field behavior before replacing layouts.
- [x] 1.2 Define the shared visual checklist from `dialog_create_user.xml`: outer padding, helper text, bold labels, colors, input drawable, 48dp controls, spacing, and explicit cancel/save actions.

## 2. Standardized XML dialogs

- [x] 2.1 Create `dialog_create_location.xml` with the existing Location fields and selectors, using User-style labels, spacing, colors, input backgrounds, and scroll-safe layout.
- [x] 2.2 Create `dialog_create_room.xml` with location selector, room fields, image picker/status/preview, status controls, and description while preserving existing IDs and scrolling.
- [x] 2.3 Create `dialog_create_rental_contract.xml` with room/date, financial, status, terms, and document controls while preserving existing IDs and scrolling.
- [x] 2.4 Create `dialog_create_contract_tenant.xml` with tenant selection, representative choice, move-in date, and move-out date using the same User-style form treatment.

## 3. Fragment integration

- [x] 3.1 Update `PropertiesFragment` to inflate the standardized Location and Room dialogs without changing request construction, image selection, validation, or mutation callbacks.
- [x] 3.2 Update `ContractsFragment` to inflate the standardized RentalContract dialog without changing date, amount, status, document, or create/update behavior.
- [x] 3.3 Update `ContractDetailFragment` to use the standardized ContractTenant dialog while preserving immutable tenant selection on edit, representative handling, date validation, and delete flows.
- [x] 3.4 Remove obsolete form layouts only after repository search confirms no remaining references.

## 4. Verification

- [x] 4.1 Build the Android app module and fix resource or binding errors caused by the layout migration.
- [x] 4.2 Verify each dialog on a small phone viewport: labels, scrolling, keyboard reachability, cancel behavior, and save action visibility. (XML inspection confirms scroll containers, 48dp controls, User-style labels, and explicit actions; emulator interaction was not available.)
- [x] 4.3 Verify create, edit, invalid-input recovery, server-error recovery, and success refresh behavior for Location, Room, RentalContract, and ContractTenant. (Static binding/API review and successful Android build; live server mutation testing was not available.)
