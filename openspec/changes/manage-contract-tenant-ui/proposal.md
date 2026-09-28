## Why

Rental contracts now have CRUD screens, but users cannot manage the tenants assigned to an individual contract. ContractTenant already has a backend CRUD API and a composite identity, so the missing capability should be added inside the contract detail screen rather than as a separate disconnected collection.

## What Changes

- Add a ContractTenant section to `ContractDetailFragment` showing tenants assigned to the current contract.
- Load assignments and tenant profiles, join them by `tenant_id`, and display tenant name and useful contact context.
- Add ContractTenant create, edit, and delete actions from the contract detail screen.
- Allow editing representative flag, move-in date, and move-out date while keeping backend-controlled status read-only.
- Allow a tenant to participate in multiple different contracts while preventing duplicate assignment within the same contract.
- Enforce at most one representative per contract by automatically clearing the previous representative in the same transaction when a new one is selected.
- Permit a contract to have no representative after an assignment is unselected or deleted.
- Add Vietnamese loading, empty, validation, conflict, success, and relationship-error feedback consistent with `UI_UX_DESIGN.md`.

## Capabilities

### New Capabilities

- `contract-tenant-ui`: Contract detail management of assigned tenants, including add, edit, delete, representative selection, and date handling.

### Modified Capabilities

- `rental-resource-crud`: Clarify ContractTenant uniqueness, multi-contract tenant participation, and single-representative transaction behavior.

## Impact

- Backend ContractTenant repository, service, tests, and representative-selection transaction behavior under `Source/BE`.
- Android ContractTenant DTOs, Retrofit methods, contract detail layout/fragment, tenant joining, dialogs/forms, and focused tests under `Source/Rentaly_Management/app`.
- Existing `/be/contract-tenants` and `/be/user/manage` APIs are reused; no database schema migration is expected.
- Existing contract detail UI, Vietnamese content rules, native controls, and destructive-action confirmation patterns remain the visual baseline.
