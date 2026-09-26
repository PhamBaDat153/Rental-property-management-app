## Context

The backend already contains JPA entities for `Location`, `Room`, `RentalContract`, `ContractTenant`, and `Tenant`. Location has a partial controller/service/DTO implementation, while room and contract services/controllers are not implemented. The entities have bidirectional associations, composite keys, database uniqueness constraints, and foreign-key dependencies that must not leak directly through JSON serialization.

## Goals / Non-Goals

**Goals:**

- Establish one consistent DTO-based CRUD style for all four resource areas.
- Keep relationship validation and deletion guards in service transactions rather than relying only on database exceptions.
- Preserve the existing entity model and endpoint conventions where practical.
- Return stable validation, not-found, and conflict responses.

**Non-Goals:**

- Authentication or authorization redesign.
- A complete rental business workflow, payment processing, invoice management, or document upload.
- Pagination, filtering, or search beyond the basic CRUD contract.
- Android UI implementation in this change.

## Decisions

### Use resource-specific request and response DTOs

Each resource gets explicit request and response shapes. Requests contain scalar fields and relationship IDs; responses contain flattened IDs and scalar values. This is preferred over returning entities because the current model is bidirectional and includes collections that could recurse or expose unrelated data.

### Use multipart requests for binary files

Room create/update endpoints accept multipart JSON plus image parts. Contract create/update endpoints accept multipart JSON plus an optional document part. Uploaded room images are stored as `RoomImage` rows and returned as URL strings; contract documents are stored as `RentalContract.document_url`. The existing Cloudinary uploader is reused for image and auto-detected document resources rather than adding another storage dependency.

Only PDF and Word MIME types/extensions are accepted for contract documents. Image content type validation is required for room image parts. Upload failures abort the enclosing transaction and return a server error rather than persisting a partially configured resource.

### Keep contract-tenant assignment as a nested resource boundary

`ContractTenant` remains a separate persistence entity, but its API uses the composite contract/tenant identity explicitly. Contract creation does not implicitly create assignments. This keeps basic CRUD predictable and avoids inventing an unrequested rule for how many tenants a new contract requires.

### Validate at the HTTP boundary and enforce relationships in services

Bean validation handles null, blank, size, range, and date-shape checks on request DTOs. Services resolve referenced IDs, enforce uniqueness and deletion guards, and execute multi-record operations transactionally. Database constraints remain the final protection against races and invalid state.

### Prefer explicit conflict checks before deletion

Before deleting a location, room, contract, tenant assignment, or other referenced record, the service checks current repository relationships and returns a domain conflict. This produces a useful API response instead of depending on a vendor-specific foreign-key exception.

### Preserve the existing endpoint family while normalizing behavior

The current `/be/locations` endpoint family is retained. New resource endpoint names should follow the same `/be/<resource>` convention. Existing null-returning service methods can be normalized behind controllers without changing entity identifiers or database table names.

### Test service rules and controller serialization separately

Service tests cover missing references, duplicate keys, date/range rules, and deletion guards. Controller tests cover request validation, HTTP status codes, DTO response shape, and absence of recursive or sensitive fields. Repository tests are added only where a derived query or explicit existence query carries behavior not already covered by the service tests.

## Risks / Trade-offs

- [Risk] Existing database mappings and entity column names use snake_case fields and inconsistent defaults -> Mitigation: reuse current table/entity mappings and verify generated SQL/schema compatibility before implementation is considered complete.
- [Risk] Foreign-key dependencies may grow beyond the currently modeled guards -> Mitigation: map all current references before implementing deletion and return a conflict for any known dependent record.
- [Risk] Requiring `status` in every request may conflict with entity defaults -> Mitigation: decide the API contract explicitly in DTO validation; use a documented default only if the request specification permits omission.
- [Risk] Contract and tenant assignment lifecycle rules are intentionally basic -> Mitigation: keep assignment CRUD independent and record advanced representative/overlap rules as future scope rather than silently enforcing them.
- [Risk] No pagination may produce large list responses -> Mitigation: keep the initial list contract simple and add pagination as a separate capability when actual usage requires it.
- [Risk] Cloudinary upload succeeds before a later database operation fails, leaving an orphaned remote file -> Mitigation: keep the database operation transactional and accept remote cleanup as a follow-up concern; do not report a successful resource response when persistence fails.
- [Risk] Multipart clients may send JSON with an incorrect content type -> Mitigation: document the required JSON part content type and return a validation error for malformed multipart requests.

## Migration Plan

1. Add DTOs, repositories, services, controllers, and exception mappings without changing existing table names.
2. Run backend tests against an isolated database/schema and verify existing user/tenant behavior remains intact.
3. Deploy backend endpoints and validate existing location clients against the normalized response/error behavior.
4. Roll back application code independently if needed; no destructive schema migration is planned.

## Open Questions

- Whether status should be required in create/update request payloads or default server-side for all resources can be finalized during implementation without changing the resource boundaries or validation categories.
