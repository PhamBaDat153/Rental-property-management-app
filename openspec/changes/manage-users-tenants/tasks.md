## 1. Backend Data Model And Persistence

- [x] 1.1 Make every user-entered `Tenant` field nullable, including `full_name`, while retaining required tenant identifier and creation timestamp; verify entity metadata and the initial SQL schema both permit an empty tenant profile.
- [x] 1.2 Add repository support for finding users by case-insensitive partial matches across tenant `full_name`, `phone`, and `identity_number`; verify list and no-match repository/service tests.
- [x] 1.3 Add a repository query for detecting `ContractTenant` references by tenant identifier; verify a contracted and uncontracted tenant are distinguished correctly.

## 2. Backend User-Tenant API

- [x] 2.1 Define request/response DTOs for user creation and user-tenant results, excluding password hashes and preventing recursive user/tenant serialization; verify JSON shape with controller tests.
- [x] 2.2 Implement transactional user creation that creates exactly one tenant with all profile fields null when omitted; verify invalid requests leave neither record persisted.
- [x] 2.3 Implement list/search and detail service/controller endpoints; verify all tenant fields, including nulls, are returned and unknown identifiers produce not-found responses.
- [x] 2.4 Implement deletion with a contract-reference guard; verify successful deletion removes an unreferenced user and tenant, while a referenced tenant returns HTTP 409 and leaves all data unchanged.
- [x] 2.5 Map validation, not-found, conflict, and persistence failures to stable API error responses; verify clients receive actionable messages for each failure class.

## 3. Android Management Screen

- [x] 3.1 Extend Retrofit API definitions and Android DTOs for create, list/search, detail, and delete operations; verify the app compiles against the backend response shapes.
- [x] 3.2 Complete `fragment_user_manage.xml` and add the user/tenant list adapter with null-safe display placeholders; verify the screen renders empty tenant profiles without crashes.
- [x] 3.3 Implement loading, search, refresh, and request error states in `UserManageFragment`; verify opening the screen and changing the search term updates the displayed results.
- [x] 3.4 Implement the add-user flow using the required user credentials and refresh the list after success; verify a newly created item shows an empty tenant profile.
- [x] 3.5 Implement item-click detail display for all tenant fields and delete confirmation; verify detail values and null placeholders are shown correctly.
- [x] 3.6 Handle successful deletion, not-found responses, and HTTP 409 contract conflicts without removing an undeletable item; verify the backend error message is visible to the landlord.

## 4. Verification And Integration

- [x] 4.1 Run backend unit/integration tests for creation, nullable tenant fields, search, detail, DTO serialization, and deletion protection; verify the test suite passes.
- [x] 4.2 Build and test the Android app with the management screen changes; verify the relevant unit/UI checks pass and no existing login/navigation behavior regresses.
- [ ] 4.3 Exercise the end-to-end flow against the development MySQL database: create user, list/search, view details, delete an unreferenced user, and reject deletion of a contracted tenant; verify database state after each operation.
