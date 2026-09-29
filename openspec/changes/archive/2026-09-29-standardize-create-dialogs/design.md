## Context

The existing User form is a compact XML layout with a short helper message, bold field labels, consistent `user_detail_card` input backgrounds, 48dp minimum controls, and predictable vertical spacing. Location, Room, and RentalContract currently use separate `form_*.xml` layouts with mixed padding and card/group styles. ContractTenant builds much of its form programmatically inside `ContractDetailFragment`.

## Goals / Non-Goals

**Goals:**

- Use dedicated XML-backed dialog layouts for all four target models.
- Reuse the User dialog's existing colors, input background, spacing rhythm, label hierarchy, and button semantics.
- Keep current Java view IDs or update all bindings consistently so API and validation code remains behaviorally unchanged.
- Keep long Room and RentalContract forms scrollable and retain existing image/document controls.

**Non-Goals:**

- No backend, DTO, Retrofit, database, navigation, or dependency changes.
- No redesign of the already standardized User, Service, Meter, MeterReading, Invoice, Maintenance, or Announcement dialogs.
- No change to required fields, validation rules, endpoint payloads, mutation timing, or success/error messages.

## Decisions

### Use one layout per target model

Create `dialog_create_location.xml`, `dialog_create_room.xml`, `dialog_create_rental_contract.xml`, and `dialog_create_contract_tenant.xml`. This keeps model-specific controls explicit while making each layout independently reviewable. A single generic form was rejected because the four models have different selectors, file/image actions, and validation states.

### Treat `dialog_create_user.xml` as the visual baseline

Copy the baseline's outer padding and label/input treatment rather than introducing new styles or dimensions. Existing semantic colors and `user_detail_card`/`edittext_background` drawables will be reused. New styling abstractions are unnecessary for four small layouts and would risk changing the established User appearance.

### Preserve existing IDs and bindings

The new layouts will retain IDs already referenced by `PropertiesFragment` and `ContractsFragment`. ContractTenant's Java-created controls will either receive matching IDs in the XML layout or have the smallest possible binding adjustment in `ContractDetailFragment`; request construction and callbacks remain in the fragment.

### Keep long dialogs scrollable

Room and RentalContract layouts will use a vertical `ScrollView` with `fillViewport` where appropriate. Location and ContractTenant will use the same approach if their final control count requires it. This follows the UI guide and avoids fixed-height dialog content on small devices.

### Standardize create and edit through the same layout

Each fragment will continue passing either null or the current model to the same form method. The layout is presentation-only; prefill, validation, and create/update selection stay in the existing Java flow.

## Risks / Trade-offs

- [Risk] Renaming or losing a view ID can cause runtime null references. -> Mitigation: inventory every `findViewById` call before replacing layouts and verify the Android module compiles.
- [Risk] Long forms may still be awkward inside an AlertDialog. -> Mitigation: retain vertical scrolling and keep actions explicit; move to dedicated screens only if a later requirement changes the interaction model.
- [Risk] ContractTenant currently uses programmatic controls and may have behavior coupled to generated IDs. -> Mitigation: preserve the current control semantics and verify add/edit date, representative, duplicate, and delete flows manually.
- [Risk] Reusing User visuals without a shared style can leave minor XML duplication. -> Mitigation: prefer the smallest change now; introduce shared styles only if repeated drift appears across more dialogs.

## Migration Plan

1. Add the four XML layouts while preserving existing binding IDs.
2. Switch the three fragment form inflations and the ContractTenant form to the new layouts.
3. Remove obsolete form layouts only after all references are gone, or retain them temporarily if another caller still uses them.
4. Build the Android module and manually verify create, edit, cancel, validation failure, scrolling, and successful mutation for each model.
5. Roll back by restoring the previous layout inflation paths; no server or database rollback is required.
