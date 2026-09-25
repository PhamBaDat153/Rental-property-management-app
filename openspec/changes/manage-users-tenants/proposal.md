## Why

The Android user-management screen is currently only a shell, while the backend exposes no lifecycle API for managing users and their tenant profiles. Landlords need one consistent flow to create users, initialize empty tenant profiles, browse and search tenants, inspect details, and remove users safely.

## What Changes

- Add a user and tenant management capability spanning the Spring backend and Android app.
- Create a tenant record automatically when a user is created; every tenant profile field may initially be `null`.
- Provide list, search, detail, create, and delete operations for users with their tenant profiles.
- Search tenant profiles by `full_name`, `phone`, or `identity_number`.
- Return a clear conflict error and keep data unchanged when deletion is requested for a tenant referenced by a rental contract.
- Use API DTOs so the bidirectional `User`/`Tenant` relationship does not create recursive JSON responses.
- Complete the Android management screen with a list, search input, add flow, detail view, delete confirmation, and backend error feedback.

## Capabilities

### New Capabilities

- `user-tenant-management`: Manage users and their one-to-one tenant profiles across the backend API and Android management screen.

### Modified Capabilities

None.

## Impact

- Backend entities, repositories, services, controllers, DTOs, validation, and database schema constraints for `Tenant.full_name`.
- Backend contract-tenant lookup used to protect tenants that are already referenced by rental contracts.
- Android `UserManageFragment`, its layout, Retrofit API definitions, DTOs, list adapter, and create/detail UI.
- Backend and Android tests for lifecycle, search, serialization, deletion conflicts, and UI/API behavior.
