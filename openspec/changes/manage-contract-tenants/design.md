## Context

The backend already exposes `/be/contract-tenants` CRUD using a composite key of `contract_id` and `tenant_id`. Requests contain the two immutable identifiers, representative flag, and optional move-in/move-out dates; responses also contain read-only status and timestamps. The current service prevents duplicate assignment within the same contract and validates date order, but it does not enforce a single representative per contract. The Android app has contract detail UI and a tenant-management endpoint at `/be/user/manage`, but no ContractTenant DTOs, Retrofit methods, or assignment section.

## Goals / Non-Goals

**Goals:**

- Embed assignment management in the existing `ContractDetailFragment` rather than introducing a separate top-level navigation destination.
- Resolve assignment tenant IDs against managed tenant profiles for readable rows and selection controls.
- Enforce one representative at most per contract in the backend transaction while allowing zero representatives.
- Preserve immutable composite identifiers and support tenant reuse across different contracts.
- Follow the existing Android XML/card/list/form patterns and explicit Vietnamese loading, empty, validation, conflict, and delete-confirmation states.

**Non-Goals:**

- No redesign of the tenant profile management screen.
- No change to the ContractTenant database key or migration.
- No editing of ContractTenant status; it remains backend-managed and read-only.
- No cross-contract tenant occupancy rule, because one tenant may be assigned to multiple contracts.
- No assignment history, bulk assignment, pagination, or separate ContractTenant list screen.

## Decisions

### Manage assignments inside contract detail

The detail screen will load all assignments and filter by the current `contract_id`, then load managed tenant profiles and join by `tenant_id`. This keeps the user's context anchored to one contract and avoids a new navigation concept. A dedicated global assignment screen was considered but would force users to repeatedly identify the contract and would duplicate contract context.

### Use DTOs and native Retrofit calls

Add Android models for ContractTenant request/response and Retrofit methods matching the existing endpoint paths. Create/update sends JSON; delete uses the composite path. The UI uses the tenant list already exposed by `/be/user/manage`, rather than a new tenant endpoint.

### Enforce representative replacement in the backend

Add a repository query for representative assignments by contract and, inside the existing transactional create/update service methods, set any previous representative to false before saving the requested representative. The lookup is scoped by `contract_id`, so the same tenant's assignments in other contracts are unaffected. No representative is required, so setting all assignments false or deleting the representative remains valid.

An Android-only enforcement was rejected because other API clients could create multiple representatives. A conflict response was also rejected because selecting a replacement should be one clear user action, not a two-step repair flow.

### Keep composite IDs immutable

The edit form will expose tenant identity as read-only and allow only dates and representative state to change. To change tenant identity, the user deletes the old assignment and adds a new one. This matches the backend's existing conflict rule and avoids attempting to mutate an embedded primary key.

### Use an assignment row plus a compact form

Each row will show tenant name, phone/email context where available, move-in/move-out dates, representative text, and read-only status. Add/edit uses a native Spinner or selection control for tenant, date controls, and a RadioButton/checkbox-style representative choice. Delete is separated and confirmed. Missing tenant profiles remain visible as `Chưa cập nhật` rather than being silently removed.

## Risks / Trade-offs

- [Risk] Loading assignments and tenant profiles independently can leave unresolved tenant IDs. -> Mitigation: render the assignment with placeholders and show a recoverable tenant-data error rather than hiding it.
- [Risk] Concurrent clients can select representatives at the same time. -> Mitigation: perform replacement in one backend transaction and add a repository/service test for the invariant; database-level locking can be considered if concurrency becomes measurable.
- [Risk] A global tenant list may be large. -> Mitigation: reuse the current endpoint and filter the Spinner labels locally for this scope; server-side tenant search remains out of scope.
- [Risk] Deleting the representative leaves no representative. -> Mitigation: explicitly support and label `Chưa chọn người đại diện` as a valid state.
- [Risk] Composite IDs prevent changing the tenant in edit. -> Mitigation: make the tenant control read-only during edit and provide delete-plus-add guidance.

## Migration Plan

1. Add backend representative query, transactional replacement logic, and service tests while preserving existing endpoint paths.
2. Add Android ContractTenant DTOs and Retrofit methods.
3. Add assignment section, row layout, empty/loading/error states, and add/edit/delete controls to contract detail.
4. Join assignment tenant IDs to managed tenant profiles and validate dates before create/update.
5. Verify representative replacement, no-representative state, duplicate assignment conflict, tenant reuse across contracts, delete confirmation, and unresolved tenant display.
6. Roll back by removing the detail assignment section and reverting the representative service/query changes; no schema rollback is required.

## Open Questions

None. Representative replacement, no representative, tenant reuse, immutable identifiers, and status read-only behavior are confirmed decisions.
