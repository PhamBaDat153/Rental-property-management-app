## Why

The user detail screen currently presents related information as a long sequence of loose views, with inconsistent spacing, `px` dimensions, and limited visual hierarchy. A professional minimal redesign will make the profile easier to scan on different Android devices while preserving the existing management actions and data behavior.

## What Changes

- Reorganize the detail screen into a clear header and grouped white information sections.
- Improve typography, spacing, alignment, and responsive sizing using `dp` and `sp`.
- Reuse `background`, `primary`, `secondary`, `dark_navy`, and `white` from the existing color palette.
- Preserve all view IDs used by `UserDetailFragment`, including role/status controls and update/delete actions.
- Replace hard-coded presentation colors and unnecessary spacer layouts with palette-based styling and intentional layout spacing.
- Keep the existing loading, null-placeholder, update, delete, and navigation behavior unchanged.

## Capabilities

### New Capabilities

- `user-detail-presentation`: Provides a professional, minimal, accessible visual presentation for viewing and managing a user's profile details.

### Modified Capabilities

- None.

## Impact

- Android resource layout: `Source/Rentaly_Management/app/src/main/res/layout/fragment_user_detail.xml`.
- Android resource styling/drawables may be updated or added only where needed for card and action presentation.
- Existing `UserDetailFragment.java` bindings and Retrofit/API contracts remain unchanged.
- No backend, database, navigation, or third-party dependency changes are expected.
