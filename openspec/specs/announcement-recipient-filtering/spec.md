# announcement-recipient-filtering Specification

## Purpose

Help landlords find and select announcement recipients efficiently by filtering the existing tenant list locally by tenant name and related rental rooms without changing the announcement API.

## Requirements

### Requirement: Display locally enriched announcement recipients
The Android announcement form SHALL display each available tenant once and SHALL show the tenant's readable name together with all related room labels derived from the existing rental data.

#### Scenario: Tenant has one related room
- **WHEN** a tenant has an assignment connected to a rental contract for one room
- **THEN** the recipient row shows the tenant once with that room's name or code

#### Scenario: Tenant has multiple related rooms
- **WHEN** a tenant has assignments connected to multiple rooms
- **THEN** the recipient row shows the tenant once with all related room labels and does not duplicate the recipient choice

#### Scenario: Tenant has no resolved room
- **WHEN** a tenant profile cannot be connected to a room through the available contract data
- **THEN** the recipient remains selectable with a clear unavailable-room label rather than being silently removed

### Requirement: Filter recipients by name or room
The Android announcement form SHALL filter the displayed recipient rows by case-insensitive tenant name search and by a selected room, and clearing filters SHALL restore all loaded recipients.

#### Scenario: Search by tenant name
- **WHEN** the user enters a name query
- **THEN** only recipient rows whose tenant name contains the query remain visible

#### Scenario: Filter by room
- **WHEN** the user selects a room
- **THEN** only recipient rows associated with that room remain visible

#### Scenario: Combine filters
- **WHEN** the user enters a name query and selects a room
- **THEN** only recipients matching both filters remain visible

#### Scenario: Clear filters
- **WHEN** the user clears the name query and selects the all-rooms option
- **THEN** the complete loaded recipient list becomes visible again

### Requirement: Select all visible recipients
The Android announcement form SHALL provide a select-all action scoped to the currently filtered recipient rows and SHALL preserve the selected state of recipients when filters change.

#### Scenario: Select all visible rows
- **WHEN** the user activates `Chọn tất cả` while a filter is applied
- **THEN** every currently visible recipient is selected and no hidden recipient is added

#### Scenario: Unselect all visible rows
- **WHEN** the user deactivates `Chọn tất cả` for the current filter
- **THEN** the currently visible recipients are unselected while selections outside the current filter remain unchanged

#### Scenario: Submit selected recipients
- **WHEN** the user saves a valid announcement
- **THEN** the request contains the unique selected user IDs in `recipient_ids`, with no duplicate ID caused by room membership

### Requirement: Keep filtering client-side and recoverable
The filtering workflow SHALL reuse existing Android APIs and SHALL keep the announcement form usable when related room, contract, or assignment data cannot be loaded.

#### Scenario: Related data loading fails
- **WHEN** rooms, contracts, or contract-tenant data cannot be loaded
- **THEN** the form shows an actionable Vietnamese error or fallback state and does not send a misleading room mapping as current data

#### Scenario: No recipient matches
- **WHEN** active filters match no recipient
- **THEN** the form shows a clear empty result message and allows the user to change or clear the filters
