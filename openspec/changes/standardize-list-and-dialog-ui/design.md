## Context

The project is an Android Java/XML application with multiple fragments that independently define headers, cards, list rows, dialogs, and state views. `dialog_create_user.xml` and the existing User management flow provide the strongest local visual baseline. `UI_UX_DESIGN.md` additionally requires native controls, semantic colors, 48dp targets, logical XML order, Vietnamese actionable feedback, no color-only state communication, and responsive vertical scrolling.

## Goals / Non-Goals

**Goals:**

- Make all management lists and dialogs feel like one application rather than separate feature prototypes.
- Reuse existing semantic colors, drawables, IDs, adapters, and native controls wherever possible.
- Normalize headers, surfaces, list rows, form labels, input backgrounds, spacing, state messages, and accessibility metadata.
- Keep domain-specific content and current interaction flows intact.

**Non-Goals:**

- No API, database, DTO, dependency, navigation graph, or business-rule changes.
- No redesign of domain information hierarchy beyond the presentation consistency needed for lists and dialogs.
- No replacement of native Android controls with a new component framework.
- No speculative shared UI framework or abstraction layer unless repeated code makes the smallest safe change clearly necessary.

## Decisions

### Use the User dialog as the form baseline

Apply the established User pattern: 24dp horizontal content padding, helper text, bold 14sp labels, semantic text colors, existing input background, 48dp controls, and explicit Vietnamese actions. Existing domain fields and IDs remain authoritative.

### Normalize layouts, not domain logic

Update XML first and keep fragment callbacks, adapters, validation, and API code unchanged unless an ID or required container must be adjusted. This limits regression risk and ensures the change remains presentation-focused.

### Use a small set of repeatable list states

Management fragments will use the same visual order:

```text
[back]  Screen title                         [primary action]
        loading / error / empty feedback
        content surface
          list rows or domain card content
```

Each feature keeps its own IDs and text, while shared layout characteristics use existing resources rather than introducing a new UI dependency.

### Preserve scroll behavior explicitly

Dialogs and detail/list content with dynamic or long text remain inside flexible vertical containers and `ScrollView` where required. Fixed-height text-heavy containers and horizontal overflow are avoided according to the UI guide.

### Treat accessibility as part of visual parity

Icon-only controls retain or gain content descriptions, status text is present alongside color, labels remain adjacent to controls, and touch targets stay at least 48dp. These are required outcomes, not optional polish.

## Risks / Trade-offs

- [Risk] Many independent layouts can drift again. -> Mitigation: use `dialog_create_user.xml` and `UI_UX_DESIGN.md` as review checklists and keep changes local and explicit.
- [Risk] Layout ID changes can break fragment bindings. -> Mitigation: preserve IDs and run repository reference searches plus the Android build after each layout group.
- [Risk] Broad visual changes may alter perceived screen density. -> Mitigation: keep existing information and actions, change only spacing, surfaces, labels, and state hierarchy required for consistency.
- [Risk] Long forms remain complex inside dialogs. -> Mitigation: preserve scrollable layouts and do not migrate to new navigation patterns in this presentation change.

## Migration Plan

1. Inventory all list/dialog layout IDs and fragment references.
2. Normalize common list screens and then remaining dialogs in small domain groups.
3. Verify each group with XML/reference checks and the Android build.
4. Run focused unit tests and manually review small viewport, large text, loading, empty, error, keyboard, and long-content states.
5. Roll back by restoring the previous XML layouts; no server or database rollback is required.
