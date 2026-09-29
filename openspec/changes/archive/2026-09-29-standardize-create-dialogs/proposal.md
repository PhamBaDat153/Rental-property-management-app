## Why

Create and edit flows for Location, Room, RentalContract, and ContractTenant do not use the same XML-backed visual language as the existing User dialog. Their forms use inconsistent padding, labels, field backgrounds, and grouping, making related landlord workflows harder to scan and maintain.

## What Changes

- Add XML-backed create/edit dialog layouts for Location, Room, RentalContract, and ContractTenant based on the existing `dialog_create_user.xml` structure.
- Standardize dialog content padding, visible field labels, spacing, text colors, input backgrounds, minimum touch heights, and Vietnamese helper text.
- Preserve every model's existing field set, validation rules, API payloads, navigation, file/image selection, and create/edit behavior.
- Update the affected fragments to inflate the standardized layouts while retaining existing callbacks and mutation feedback.
- Keep dialogs scrollable where the model has more fields than a phone viewport can display.

## Capabilities

### New Capabilities

- `standardized-create-dialogs`: Consistent User-style create/edit dialogs for the remaining rental-management models.

### Modified Capabilities

- None. This is a presentation and form-structure standardization; model behavior and API contracts remain unchanged.

## Impact

- Android XML layouts under `Source/Rentaly_Management/app/src/main/res/layout`.
- `PropertiesFragment`, `ContractsFragment`, and `ContractDetailFragment` form inflation and view binding under `Source/Rentaly_Management/app/src/main/java`.
- No backend, database, Retrofit endpoint, DTO, or dependency changes.
