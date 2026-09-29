## MODIFIED Requirements

### Requirement: Provide utilities navigation
The utilities area SHALL provide separate entry points for Hóa đơn, Bảo trì, and Danh mục dịch vụ. Meter management SHALL remain reachable from the room detail workflow and SHALL not be presented as a utilities entry point.

#### Scenario: Display utilities actions
- **WHEN** the user opens the utilities screen
- **THEN** the application displays Hóa đơn, Bảo trì, and Danh mục dịch vụ actions with clear Vietnamese labels and accessible touch targets

#### Scenario: Open utility destinations
- **WHEN** the user selects Hóa đơn, Bảo trì, or Danh mục dịch vụ
- **THEN** the application navigates to the matching management screen without changing the selected room or meter state

#### Scenario: Open meter management from a room
- **WHEN** the user chooses meter management from room detail
- **THEN** the application opens meter management for that room with its `room_id` context
