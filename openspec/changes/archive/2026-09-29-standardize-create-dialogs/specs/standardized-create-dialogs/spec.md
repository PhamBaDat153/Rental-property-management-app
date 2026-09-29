## Purpose

Provide a consistent, readable Android create/edit dialog experience for rental-management models that currently use visually different forms, while preserving their existing fields and behavior.

## ADDED Requirements

### Requirement: Use the User dialog visual language
The Android application SHALL present Location, Room, RentalContract, and ContractTenant create/edit forms with the same visual language as the existing User create dialog, including consistent content padding, field labels, spacing, text colors, input surfaces, and minimum touch targets.

#### Scenario: Open a supported create dialog
- **WHEN** the user starts creating a Location, Room, RentalContract, or ContractTenant record
- **THEN** the dialog shows a labeled, vertically ordered form using the User dialog's visual treatment and Vietnamese helper text where applicable

#### Scenario: Open a supported edit dialog
- **WHEN** the user edits an existing supported record
- **THEN** the dialog uses the same standardized presentation while pre-filling all existing editable values

### Requirement: Preserve model-specific form behavior
The standardized dialogs SHALL retain the complete existing field set and controls required by each model, including selectors, status controls, optional values, image/document actions, and scrolling for content exceeding the viewport.

#### Scenario: Submit a valid form
- **WHEN** the user submits valid values from any standardized dialog
- **THEN** the application sends the same model request and mutation operation as before and reports success using the existing flow

#### Scenario: Correct invalid input
- **WHEN** validation fails or the server rejects a submitted value
- **THEN** the dialog remains recoverable, preserves valid entered values, and displays the existing actionable Vietnamese feedback

### Requirement: Keep dialog interaction accessible
The standardized dialogs SHALL keep controls readable and reachable on small screens, provide meaningful labels for fields and icon-only actions, and keep cancel and save actions explicit and distinct.

#### Scenario: Use a dialog on a small viewport
- **WHEN** the dialog content is taller than the available phone viewport
- **THEN** the user can scroll through the form without clipping fields or losing access to the dialog actions

#### Scenario: Cancel a form
- **WHEN** the user selects the cancel action
- **THEN** the dialog closes without submitting or changing the current record
