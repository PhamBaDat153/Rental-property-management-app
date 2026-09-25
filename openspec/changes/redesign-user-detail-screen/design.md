## Context

The Android detail screen is a `ScrollView` backed by `fragment_user_detail.xml`. `UserDetailFragment` binds directly to the layout's existing IDs, fills profile values through `TextView`s, uses disabled gender radio buttons for display, and uses role/status radio groups for updates. The current layout mixes `dp` and `px`, includes empty spacer containers, and uses a hard-coded delete color. See `proposal.md` and `specs/user-detail-presentation/spec.md` for the motivation and observable requirements.

## Goals / Non-Goals

**Goals:**

- Establish a compact visual hierarchy with a header, profile summary, information cards, management controls, and clear action placement.
- Keep the layout compatible with the existing Java fragment by preserving every referenced view ID and required view type.
- Use the existing palette and density-independent Android dimensions.
- Make incomplete profiles and long values readable within the existing vertical scroll behavior.

**Non-Goals:**

- No changes to API models, Retrofit calls, backend behavior, navigation, or fragment logic.
- No new UI framework, dependency, theme migration, or broad application redesign.
- No change to which fields are editable or which management actions are available.

## Decisions

### Use one scrollable layout with grouped cards

Keep the root `ScrollView` and one vertical content container, but replace loose sequential fields and empty spacer layouts with intentional sections. White rounded containers will separate profile, personal, identity/address, emergency, and management content against `@color/background`.

Using multiple fragments or a new RecyclerView would add lifecycle and binding complexity without improving this fixed-size detail form.

### Preserve existing IDs and control types

Retain IDs such as `user_full_name`, `user_phone`, `user_gender`, `user_detail_role`, `user_detail_status`, `user_detail_update`, and `user_detail_delete`. Retain `RadioGroup` and `RadioButton` types for the controls because the current fragment calls `getCheckedRadioButtonId()` and checks individual buttons.

Changing to a new binding model or replacing controls with custom widgets is rejected because it would expand the scope into Java changes and increase regression risk.

### Use resource-backed styling with the existing palette

Use `@color/background` for the page, `@color/white` for cards, `@color/dark_navy` for primary values, `@color/secondary` for supporting labels, and `@color/primary` for headings and the primary update action. Any supporting rounded shape or style resource should reference these colors rather than introduce one-off hex values.

The delete action should use an existing palette color or a deliberately defined resource only if the palette cannot provide sufficient destructive-action contrast; it must not retain the current inline `#F44336` value.

### Normalize dimensions and typography

Replace pixel-based margins, padding, and text sizes with `dp` and `sp`. Use consistent page padding, card spacing, section heading sizes, and value text sizes. Long values should wrap naturally, while action buttons remain full width and touch-friendly.

### Keep display-only and editable controls visually distinct

Gender remains a display-only group because the current controls are disabled and the fragment only sets their checked state. Role and status remain editable management controls and should be grouped near the action buttons so the update operation's scope is clear.

## Risks / Trade-offs

- [Risk] Replacing or renaming an existing ID can cause runtime crashes in `UserDetailFragment` -> Preserve all referenced IDs and verify the Android build plus layout inflation.
- [Risk] A card-heavy layout can become too tall on small screens -> Keep the existing `ScrollView`, avoid nested scrolling containers, and verify the full action area is reachable.
- [Risk] Existing drawable or theme behavior may override XML tint and text styling -> Inspect rendered resources and verify the primary, secondary, and destructive states on an emulator or device.
- [Risk] The existing color palette has no dedicated destructive color -> Prefer an existing contrast-safe palette resource; if a new resource is unavoidable, document it as a named color rather than embedding a hex value in the layout.

## Migration Plan

1. Update the Android layout and only the supporting resource files required for grouped cards and consistent actions.
2. Build the Android module and inflate the detail screen with representative complete and incomplete user data.
3. Verify back navigation, role/status selection, update, delete, null placeholders, and scrolling behavior.
4. Roll back by restoring the prior resource files; no data, API, or database migration is required.
