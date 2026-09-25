## Purpose

Provide landlords with a complete, safe way to manage application users and the tenant profiles associated with them.

## ADDED Requirements

### Requirement: User creation initializes an empty tenant profile
The system SHALL create a user and exactly one associated tenant profile as one successful operation. Every tenant profile field other than its system-managed identifier and timestamps SHALL be allowed to be `null` when the profile is initialized.

#### Scenario: Create user with empty tenant profile
- **WHEN** a valid landlord request supplies the required user credentials without tenant details
- **THEN** the system creates the user and an associated tenant profile, returns their identifiers, and represents all omitted tenant fields as `null`

#### Scenario: Reject incomplete user creation
- **WHEN** a create request omits a required user credential or violates a uniqueness rule
- **THEN** the system rejects the request without creating either the user or tenant profile

### Requirement: List and search users with tenant profiles
The system SHALL return users together with their associated tenant profile data and SHALL support filtering by a case-insensitive partial match against tenant `full_name`, `phone`, or `identity_number`.

#### Scenario: List all users
- **WHEN** the management client requests the user list without a search term
- **THEN** the system returns every manageable user with its tenant profile, including profiles whose fields are all `null`

#### Scenario: Search by tenant field
- **WHEN** the management client supplies a non-empty search term
- **THEN** the system returns users whose tenant full name, phone number, or identity number contains that term without requiring an exact match

#### Scenario: Search has no matches
- **WHEN** no tenant field contains the supplied search term
- **THEN** the system returns an empty collection with a successful response

### Requirement: View tenant details
The system SHALL allow the management client to retrieve the complete user and tenant profile for a selected user.

#### Scenario: Retrieve existing user details
- **WHEN** the client requests details for an existing user identifier
- **THEN** the system returns the user data and every tenant profile field, including `null` values

#### Scenario: Retrieve unknown user details
- **WHEN** the client requests details for an identifier that does not exist
- **THEN** the system returns a not-found error

### Requirement: Delete users safely
The system SHALL delete a user and its associated tenant profile only when that tenant is not referenced by any rental contract.

#### Scenario: Delete user without contract reference
- **WHEN** a delete request targets an existing user whose tenant has no contract-tenant reference
- **THEN** the system deletes the user and associated tenant and confirms successful deletion

#### Scenario: Reject deletion of contracted tenant
- **WHEN** a delete request targets an existing user whose tenant is referenced by a rental contract
- **THEN** the system leaves the user, tenant, and contract data unchanged and returns a conflict error explaining that the tenant cannot be deleted

#### Scenario: Delete unknown user
- **WHEN** a delete request targets an identifier that does not exist
- **THEN** the system returns a not-found error

### Requirement: Android management screen exposes the lifecycle
The Android management screen SHALL display the user/tenant list, allow searching, provide a create flow, open details when an item is selected, and require confirmation before deletion.

#### Scenario: Load and filter the list
- **WHEN** the management screen opens or the search text changes
- **THEN** it requests or filters the corresponding user/tenant results and displays empty tenant fields without crashing

#### Scenario: Create a user
- **WHEN** the landlord submits valid required user credentials
- **THEN** the Android client sends the create request, reports success, and refreshes the list to show the new user with an empty tenant profile

#### Scenario: View details
- **WHEN** the landlord taps a user/tenant item
- **THEN** the Android client opens a detail view containing all tenant fields and their current values or empty placeholders

#### Scenario: Handle delete conflict
- **WHEN** the landlord confirms deletion and the backend reports that the tenant is referenced by a contract
- **THEN** the Android client keeps the item visible and shows the backend conflict message
