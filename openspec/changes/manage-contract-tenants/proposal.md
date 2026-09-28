## Why

Rental contracts currently expose no Android workflow for managing the tenants assigned to a contract, even though the backend already provides ContractTenant CRUD. Landlords need to see, add, edit, and remove tenant assignments directly from contract detail while preserving assignment dates and representative information.

## What Changes

- Add ContractTenant management inside the existing `ContractDetailFragment`.
- Load contract-tenant assignments and tenant profiles, then join them by `tenant_id` for readable names and contact context.
- Add, edit, and delete assignment actions with date validation and confirmed deletion.
- Allow a tenant to appear in multiple different contracts while preventing duplicate assignment in the same contract.
- Enforce at most one representative tenant per contract; selecting a new representative automatically unsets the previous one in the same transaction.
- Allow contracts to have no representative tenant.
- Display ContractTenant status as read-only because the backend assigns it and the current request does not edit it.
- Keep composite assignment identifiers immutable; changing the tenant requires deleting the old assignment and creating a new one.

## Capabilities

### New Capabilities

- `rental-contract-tenant-ui`: Contract detail tenant-assignment list, forms, CRUD actions, representative rules, and tenant context.

### Modified Capabilities

- `rental-resource-crud`: Extend ContractTenant behavior with single-representative enforcement scoped per contract and explicit support for tenants appearing in multiple contracts.

## Impact

- Backend ContractTenant service, repository queries, DTO behavior, and tests under `Source/BE`.
- Android ContractTenant DTOs, Retrofit methods, contract detail layout/fragment, tenant data loading, and focused tests under `Source/Rentaly_Management/app`.
- Existing `/be/contract-tenants` and `/be/user/manage` APIs are reused; no database migration is expected.
- UI follows `UI_UX_DESIGN.md` and the existing contract detail card/action patterns.
