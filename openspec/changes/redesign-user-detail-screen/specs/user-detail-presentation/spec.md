## Purpose

Provide a clear, professional, and minimal user-detail presentation that lets landlords scan profile information and manage user status without changing existing data or navigation behavior.

## ADDED Requirements

### Requirement: Group user information by meaning

The user-detail screen SHALL present the profile header, personal information, address and identity information, emergency contact information, and management controls as visually distinct groups.

#### Scenario: Landlord views a complete profile

- **WHEN** a user detail response contains populated profile fields
- **THEN** the screen presents those fields in their corresponding labeled group with a consistent hierarchy between labels and values

#### Scenario: Landlord views an incomplete profile

- **WHEN** a user detail response contains null or blank tenant fields
- **THEN** the screen keeps the field visible in its group and displays the existing missing-value placeholder without breaking the layout

### Requirement: Use the existing visual palette consistently

The user-detail screen SHALL use the existing `background`, `primary`, `secondary`, `dark_navy`, and `white` colors for page background, accents, supporting text, primary text, cards, and actions, and SHALL NOT rely on unrelated hard-coded presentation colors.

#### Scenario: Screen renders in the application theme

- **WHEN** the user-detail screen is opened on a supported Android device
- **THEN** its backgrounds, text, section accents, and primary action use the established application palette consistently

### Requirement: Preserve user-detail behavior and bindings

The redesign SHALL preserve the existing view IDs required for loading user data, selecting role and status, navigating back, updating the user, and deleting the user.

#### Scenario: Existing detail data is loaded

- **WHEN** the detail request succeeds
- **THEN** all existing user fields, gender indicators, role controls, and status controls remain available for the current fragment logic

#### Scenario: Existing management action is used

- **WHEN** the landlord taps back, update, or delete
- **THEN** the corresponding existing action is invoked without requiring an API or navigation contract change

### Requirement: Remain readable across device sizes

The user-detail screen SHALL use density-independent dimensions and scalable text sizes, support vertical scrolling for smaller screens, and maintain readable spacing without relying on pixel-based dimensions.

#### Scenario: Screen is displayed on a small device

- **WHEN** the available viewport cannot show all profile groups at once
- **THEN** the landlord can scroll through the complete screen and no information or action is clipped

#### Scenario: Screen is displayed on a larger device

- **WHEN** the screen has additional horizontal or vertical space
- **THEN** content remains aligned and visually grouped rather than expanding arbitrary spacer areas
