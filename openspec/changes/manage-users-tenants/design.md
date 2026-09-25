## Context

The backend currently exposes only user listing and login endpoints. `User` owns a one-to-one `Tenant` through `user.tenant`, while `Tenant` is also referenced by `ContractTenant`; the current entity and SQL schema make `full_name` non-null. The Android app uses Java fragments and Retrofit, and `UserManageFragment` currently contains only a title and add button.

## Goals / Non-Goals

**Goals:**

- Provide a transactional user-plus-empty-tenant lifecycle through a small REST API.
- Keep API responses finite and stable despite the bidirectional JPA relationship.
- Support tenant-field search, detail retrieval, and contract-aware deletion.
- Give the existing Android management fragment a complete list/create/detail/delete experience.

**Non-Goals:**

- Authentication or authorization redesign; the existing management access model remains unchanged.
- Rental contract CRUD, tenant-to-contract assignment, or tenant profile editing beyond what is needed to display the detail flow.
- Pagination, offline caching, or a new Android architecture layer unless required by the existing project build.

## Decisions

### Use dedicated request and response DTOs

Controllers will accept explicit create input and return flattened user/tenant response data rather than serializing JPA entities. This avoids the `User <-> Tenant` recursion risk and keeps password fields out of management responses. Returning entities directly is rejected because it couples the API to persistence mappings and can expose internal fields.

### Create the tenant in the user service transaction

The create operation will construct both objects, associate both sides of the one-to-one relationship, and persist them as one unit. A service-level transaction provides all-or-nothing behavior; separate client calls would allow orphaned users or tenants.

### Make tenant profile columns nullable, but keep system fields required

The entity mapping and initial schema will allow all user-entered tenant fields, including `full_name`, to be null. `tenant_id` and `created_at` remain system-managed requirements. The identity unique constraint remains because MySQL permits multiple null values while still preventing duplicate complete identity pairs.

### Search with one tenant-field query

The repository will provide a case-insensitive partial-match query across `full_name`, `phone`, and `identity_number`, treating a missing search term as an unfiltered list. This matches the user-facing search behavior without loading the entire database into Android.

### Guard deletion with an existence check

Before deletion, the service will check whether `ContractTenant` contains the tenant identifier. If it does, the service raises a conflict result and performs no delete. This gives a deterministic domain error instead of relying only on a database foreign-key exception. The existing orphan-removal relationship can then remove the tenant only for users with no contract reference.

### Keep Android implementation within existing Java/Retrofit patterns

The fragment will use the existing Retrofit client, explicit DTOs, a simple list adapter, and Android dialogs/views. No new dependency or navigation graph is needed for the initial detail and create interactions; dialogs or a dedicated fragment may be selected during implementation based on the final layout complexity.

## Risks / Trade-offs

- [Risk] Existing databases may already have a `NOT NULL` constraint on `tenant.full_name` -> Include an explicit schema migration/update step and verify both entity metadata and `Database scripts/db_initial_schema.sql`.
- [Risk] Tenant deletion may fail if another future table references tenants -> Keep the service conflict check focused on current contract references and surface database failures as unsuccessful operations rather than reporting false success.
- [Risk] Search results may grow large without pagination -> Accept an unpaginated first version as the current app has no paging contract; add pagination when dataset size requires it.
- [Risk] Cleartext HTTP and unauthenticated endpoints are currently development defaults -> Do not expand this change into security redesign, but keep response DTOs free of passwords and document the existing deployment constraint.

## Migration Plan

1. Update the tenant nullability mapping and database initialization/migration SQL.
2. Deploy the backend API changes and run backend tests against the schema.
3. Ship the Android client after the API contract is available.
4. Roll back application code independently if needed; retain the nullable schema change because it is backward-compatible with existing populated tenant rows.
