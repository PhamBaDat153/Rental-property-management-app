## Context

The backend already exposes `/be/contract-tenants` CRUD with a composite key `(contract_id, tenant_id)`, assignment dates, representative flag, and backend-controlled `ACTIVE`/`INACTIVE` status. `/be/user/manage` returns tenant profile data through `UserTenantResponse`, while the Android app currently has no ContractTenant DTOs, Retrofit methods, or detail section. `ContractDetailFragment` is the established place for contract-specific management actions and already follows the scrollable card-based detail pattern.

## Goals / Non-Goals

**Goals:**

- Add ContractTenant management without introducing a separate top-level Android screen.
- Resolve assignment tenant IDs to readable tenant profiles and keep unresolved assignments actionable.
- Preserve composite IDs after creation and use the existing CRUD endpoints.
- Enforce one representative per contract atomically while allowing zero representatives and tenant reuse across contracts.
- Reuse native Spinner, RadioButton, date picker, scrollable cards, confirmation dialogs, Retrofit callbacks, and Vietnamese state feedback.

**Non-Goals:**

- No tenant profile CRUD redesign or new tenant search endpoint.
- No assignment status editing; `status` remains response-only in this workflow.
- No database migration or replacement of the composite-key model.
- No contract list redesign outside the new detail section.

## Decisions

### Manage assignments inside contract detail

`ContractDetailFragment` will load all assignments and filter by its current `contract_id`, then load managed tenant profiles and join by `tenant_id`. This keeps the user in contract context and avoids a disconnected global assignment screen. A separate `/be/contract-tenants?contractId=` endpoint was considered but is not needed for the current scope.

### Use a dedicated assignment section and compact form

The detail layout will add a white card after the contract information with loading/empty/error state, assignment rows, and a primary `Thêm người thuê` action. Each row exposes tenant identity, representative text, move-in/out dates, status, edit, and delete controls. Add/edit uses a dialog or compact XML-backed form with a tenant Spinner, representative RadioButton/RadioGroup, and native date controls.

### Keep composite identifiers immutable

The edit form will display the selected tenant but will not permit changing it. This matches the backend guard that rejects identifier changes. To assign a different tenant, the user removes the old assignment and creates a new one; this avoids hidden delete/create behavior and keeps the API semantics explicit.

### Enforce one representative in the backend transaction

`ContractTenantRepository` will provide a query for assignments marked representative for one contract. `ContractTenantServiceImplement.create/update` will clear that representative before saving the selected assignment when `is_representative` is true, inside the existing `@Transactional` method. The query is scoped by `contract_id`, so the same tenant can be representative or non-representative independently on other contracts. Setting false or deleting the only representative leaves zero representatives valid.

The alternative of enforcing this only in Android was rejected because other API clients could violate the rule and concurrent requests could create two representatives.

### Keep status read-only

The backend initializes assignment status and the request does not support changing it. Android will render status as Vietnamese text and will not expose a misleading editable control.

### Use client-side duplicate filtering plus backend conflict protection

The add form will exclude tenant IDs already assigned to the current contract where possible. The backend remains authoritative and its duplicate conflict will still be mapped to recoverable Vietnamese feedback, covering stale lists and concurrent changes.

## Risks / Trade-offs

- [Risk] Loading all assignments and all managed tenants can be larger than necessary. -> Mitigation: filter immediately by current contract and reuse existing endpoints; add scoped endpoints only if measured payload size becomes a problem.
- [Risk] Tenant profile loading can fail independently from assignment loading. -> Mitigation: show unresolved tenant labels while retaining composite-key edit/delete actions and provide retry feedback.
- [Risk] Concurrent representative updates can race. -> Mitigation: perform representative clearing and selected save in one transactional backend operation; database-level strengthening can be added later if concurrency requires it.
- [Risk] Users may delete the only representative unintentionally. -> Mitigation: confirm deletion and explicitly allow the resulting `Chưa chọn người đại diện` state rather than silently assigning another tenant.
- [Risk] Changing a tenant requires two operations. -> Mitigation: explain the immutable composite identity through clear UI labels and keep add/delete actions adjacent.

## Migration Plan

1. Add repository representative query and transactional service behavior with backend tests.
2. Add Android ContractTenant DTOs and Retrofit CRUD methods plus focused mapping/date tests.
3. Add the contract detail assignment card, row display, tenant join, loading/empty/error states, and add/edit/delete interactions.
4. Connect representative replacement, date validation, duplicate handling, and Vietnamese conflict feedback.
5. Build backend and Android modules, run unit tests, and manually verify one/no/multiple tenant, representative replacement, representative removal, duplicate, missing profile, and delete flows.
6. Roll back by removing the detail section/API client additions and reverting the representative service/query change; no schema rollback is required.

## Open Questions

None. The representative, tenant reuse, status, location, and composite-key decisions are confirmed.
