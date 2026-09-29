## Why

The Android application now has a User-style visual baseline for several forms, but list fragments, headers, detail surfaces, and some dialogs still use inconsistent spacing, cards, controls, labels, and state presentation. This makes navigation across rental-management areas feel fragmented and leaves Maintenance and other dialogs visibly behind the established design.

## What Changes

- Standardize all Android management fragment list screens and their dialogs around the User-style visual language and `UI_UX_DESIGN.md`.
- Align list headers, back/add actions, page surfaces, cards, list rows, loading indicators, empty states, and error states across Service, Meter, Invoice, Maintenance, Announcement, Properties, Contracts, and User management areas where applicable.
- Align remaining create/edit dialogs, including Service, Meter, MeterReading, Invoice, Maintenance, and Announcement, with the User dialog's labels, padding, input surfaces, spacing, colors, and accessible control sizes.
- Preserve existing view IDs, API calls, request payloads, validation, navigation, mutation behavior, and domain-specific fields.
- Keep long forms and list/detail content scrollable and usable on small Android screens.

## Capabilities

### New Capabilities

- `standardized-list-and-dialog-ui`: Consistent presentation and interaction states for Android rental-management lists and dialogs.

### Modified Capabilities

- None. This change standardizes presentation and interaction affordances without changing domain behavior or API contracts.

## Impact

- Android XML layouts under `Source/Rentaly_Management/app/src/main/res/layout`.
- Related fragment view binding only where layout changes require it under `Source/Rentaly_Management/app/src/main/java`.
- Existing drawable/color resources may be reused or minimally extended.
- No backend, database, Retrofit endpoint, DTO, navigation, or external dependency changes.
