## 1. Backend ContractTenant Rules

- [ ] 1.1 Add a repository query to find representative assignments by contract ID and verify the query is scoped only to the requested contract.
- [ ] 1.2 Update ContractTenant create/update service transactions to unset an existing representative before setting the requested assignment representative, and verify a contract can have zero or one representative.
- [ ] 1.3 Preserve duplicate protection within one contract while allowing the same tenant in different contracts, and verify both cases with service tests.
- [ ] 1.4 Add backend tests for representative replacement, no representative after unsetting/deleting, date validation, duplicate assignment, tenant reuse across contracts, and immutable composite identifiers.

## 2. Android Data And API

- [ ] 2.1 Add ContractTenant request/response DTOs with UUID identifiers, dates, representative flag, read-only status, and timestamps, and verify Gson maps date and enum values.
- [ ] 2.2 Add Retrofit methods for assignment list/create/update/delete and verify endpoint paths match `/be/contract-tenants` and composite identifiers.
- [ ] 2.3 Add tenant-assignment mapping helpers that filter by current contract ID, join tenant profiles by tenant ID, and display `Chưa cập nhật` for unresolved profiles; verify focused mapping tests.

## 3. Contract Detail UI

- [ ] 3.1 Add an assignment section to the contract detail layout with loading, empty, error, add, row, edit, representative, status, and delete controls following `UI_UX_DESIGN.md`; verify XML compilation and scroll reachability.
- [ ] 3.2 Load assignments and managed tenants alongside contract detail, coordinate states, and render only assignments for the current contract; verify unresolved tenant profiles remain visible.
- [ ] 3.3 Add a create-assignment form with tenant selection, representative choice, optional move-in/move-out dates, field validation, and Vietnamese feedback; verify invalid date order does not submit.
- [ ] 3.4 Add an edit-assignment form that keeps tenant identity immutable, edits dates/representative state, displays status read-only, and refreshes the row after success.
- [ ] 3.5 Add confirmed assignment deletion and map duplicate, not-found, conflict, and connectivity failures without leaving stale rows presented as current.
- [ ] 3.6 Make representative selection clearly communicate that choosing a new representative replaces the previous one and allow the valid no-representative state.

## 4. Verification

- [ ] 4.1 Add Android unit tests for assignment filtering, tenant joining, date validation, representative/no-representative display, and request serialization; verify tests pass.
- [ ] 4.2 Build the backend and Android modules with available repository commands; verify compilation and Android unit tests pass.
- [ ] 4.3 Manually verify contract detail flows for populated assignments, empty assignments, unresolved tenants, add, edit, representative replacement, no representative, duplicate assignment, delete confirmation, delete failure, and narrow viewport scrolling.
