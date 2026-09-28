## MODIFIED Requirements

### Requirement: Provide explicit resource states and Vietnamese feedback
The resource views SHALL represent loading, empty, request failure, validation failure, successful mutation, unavailable related data, and destructive-action confirmation states with readable Vietnamese labels and without relying on color alone. The contracts view SHALL apply these states while loading contracts and their related rooms.

#### Scenario: Show a loading or empty state
- **WHEN** a resource list request is in progress or succeeds with no records
- **THEN** the application shows the corresponding loading or empty message and does not present stale rows as current data

#### Scenario: Report API failure
- **WHEN** a resource request fails because of connectivity, validation, not-found, duplicate-code, or relationship-conflict errors
- **THEN** the application shows an actionable Vietnamese message and keeps the user on a recoverable screen

#### Scenario: Report unavailable related data
- **WHEN** a contract list cannot resolve one or more related rooms
- **THEN** the application labels the affected room context as unavailable and keeps the contract record distinguishable without exposing technical identifiers as the primary label
