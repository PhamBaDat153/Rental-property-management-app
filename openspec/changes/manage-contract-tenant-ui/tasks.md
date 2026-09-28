## 1. Backend Assignment Rules

- [x] 1.1 Add a repository query to find representative assignments by `contract_id`, and verify it is scoped only to the requested contract.
- [x] 1.2 Update ContractTenant create/update service logic to clear the previous representative and save the selected representative in one transaction; verify zero or one representative per contract and isolation across contracts.
- [ ] 1.3 Preserve duplicate composite-key and move-out-before-move-in conflicts, and add backend tests for tenant reuse across contracts, duplicate assignment, representative replacement, representative removal, and deletion.

## 2. Android Data And API

- [x] 2.1 Add ContractTenant and ContractTenantRequest DTOs matching response/request fields, and verify nullable dates, UUIDs, Boolean representative state, enum status, and timestamps map correctly.
- [x] 2.2 Add Retrofit list/detail/create/update/delete methods for `/be/contract-tenants`, and verify composite path parameters and request body fields.
- [x] 2.3 Add mapping helpers that filter assignments by current contract, join tenant profiles by `tenant_id`, identify already assigned tenants, and represent unresolved profiles as `Chưa cập nhật người thuê`.

## 3. Contract Detail UI

- [x] 3.1 Add a ContractTenant card to the contract detail layout following `UI_UX_DESIGN.md`, with loading, empty, error, add, row, edit, and delete affordances; verify scrolling and 48dp touch targets.
- [x] 3.2 Load assignments and managed tenants for the current contract, coordinate their states, and render readable tenant rows with representative text, dates, and read-only status.
- [x] 3.3 Add the create assignment form with tenant selection limited to unassigned tenants, representative choice, optional move-in/move-out dates, and field-level validation; verify duplicate and invalid-date submissions are blocked or reported.
- [x] 3.4 Add assignment editing with immutable tenant/contract identity, representative choice, optional dates, and returned-value refresh; verify selecting a new representative reflects backend replacement.
- [x] 3.5 Add confirmed assignment deletion, allow deleting the only representative, and map success, conflict, not-found, connectivity, and unresolved-profile states to Vietnamese feedback.

## 4. Verification

- [x] 4.1 Add focused Android tests for assignment filtering/joining, duplicate tenant selection, representative display, and date validation.
- [ ] 4.2 Run backend tests/build with the available Maven command or wrapper and verify representative transaction behavior.
- [ ] 4.3 Run Android unit tests and `:app:assembleDebug`, then manually verify no tenant, one tenant, multiple tenants, representative replacement, no representative, tenant reuse across contracts, missing profile, duplicate, edit, and delete flows on small and large viewports.
