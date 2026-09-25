# Rentaly UI/UX Design Guide

This document is the default visual and interaction guide for the Rentaly Android application. New screens and UI changes should follow these rules unless a feature has a documented reason to diverge.

## Design Direction

Rentaly uses a professional, calm, and minimal interface for property-management work.

The interface should feel:

- Clear before decorative
- Structured without feeling dense
- Trustworthy and business-focused
- Consistent across landlord and tenant workflows
- Comfortable on small Android devices

Prefer native Android components, simple layouts, and resource-backed styling over custom UI complexity.

### Product Character

Rentaly is an operational tool, not a marketing site. Users open it to complete tasks such as checking a room, reviewing a tenant, managing a contract, handling an invoice, or responding to maintenance. The UI should therefore optimize for recognition, confidence, and completion speed.

The visual language should communicate:

- **Order:** Related information is grouped and easy to scan.
- **Confidence:** Important values and consequences are visible before an action is confirmed.
- **Calm:** Surfaces are light, colors are restrained, and decoration does not compete with data.
- **Control:** Editable fields and destructive actions are clearly distinguished.
- **Continuity:** A user should be able to move from list to detail to action without learning a new layout pattern.

Avoid designs that feel like:

- A dashboard overloaded with equal-weight cards
- A form with no clear reading order
- A table squeezed into a phone viewport
- A collection of unrelated colored buttons
- A decorative landing page that hides operational actions

### Design Principles

1. **Clarity over density:** Show the information needed for the current decision first. Secondary information can follow below.
2. **Hierarchy over decoration:** Use size, weight, spacing, and grouping before adding icons, gradients, or shadows.
3. **Consistency over novelty:** Reuse established components and patterns across screens.
4. **State over assumption:** Loading, empty, error, unavailable, and incomplete states must be designed explicitly.
5. **Safe action over fast action:** Destructive or irreversible actions require clear labeling and confirmation.
6. **Native over custom:** Use Android controls and platform behavior unless the native pattern cannot satisfy the interaction.
7. **Content is part of design:** Vietnamese labels, placeholders, errors, and button text must be understandable without developer context.

## Color System

Use the existing colors in `Source/Rentaly_Management/app/src/main/res/values/colors.xml`.

| Resource | Value | Usage |
| --- | --- | --- |
| `background` | `#EEEEEE` | Screen background and subtle borders |
| `primary` | `#548CA8` | Main actions, active accents, section headings |
| `secondary` | `#476072` | Supporting labels, secondary actions, metadata |
| `dark_navy` | `#334257` | Primary text, important values, titles |
| `white` | `#FFFFFF` | Cards, list items, input surfaces |
| `black` | `#000000` | Use only when required by a native component |

### Color Rules

- Use `background` as the default page surface.
- Use `white` for elevated content groups and list items.
- Use `dark_navy` for values and primary readable text.
- Use `secondary` for labels and supporting information.
- Use `primary` for the main action and selected/active emphasis.
- Do not add inline hex colors in layout XML.
- Add a named color resource before introducing a new color.
- Do not use color alone to communicate an error, status, or selection.

### Semantic Color Roles

Use colors by role rather than choosing a color ad hoc for each view.

| Semantic role | Default resource | Examples |
| --- | --- | --- |
| Page surface | `background` | Root screen, scroll area |
| Content surface | `white` | Cards, list rows, dialogs |
| Primary text | `dark_navy` | Names, values, screen titles |
| Secondary text | `secondary` | Labels, metadata, helper text |
| Main emphasis | `primary` | Main button, active heading, selected state |
| Quiet border | `background` | Card outline, divider, input outline |
| System-required black | `black` | Only where a native component requires it |

If the product later needs explicit success, warning, error, or inactive colors, add semantic resources such as `status_success`, `status_warning`, `status_error`, and `status_inactive` to `colors.xml`. Do not scatter raw values through layouts.

### Contrast And Color Safety

- Text must remain readable against both `background` and `white` surfaces.
- Primary buttons must have readable text in normal and disabled states.
- A status must include a text label such as `Đang hoạt động` or `Không hoạt động`; a colored dot alone is insufficient.
- Selected and unselected radio states must remain distinguishable when viewed without color.
- Disabled controls may be visually muted, but their labels must remain legible.
- When a color is used as a border or icon tint, verify that it does not disappear against the surface behind it.

## Layout Structure

Most screens should follow this hierarchy:

```text
Screen background
  +-- Page header
  +-- Main content
  |     +-- Summary or primary information
  |     +-- Grouped content sections
  |     +-- Empty/loading/error state when needed
  +-- Primary action area
```

For detail screens, group information by meaning rather than by database structure:

```text
Header
Profile summary
Personal information
Address and identity
Related or emergency information
Management controls
Actions
```

Use a single vertical `ScrollView` when the content can exceed the viewport. Avoid nested scrolling containers unless the interaction explicitly requires them.

### Screen Templates

#### List Screen

Use this structure for users, rooms, invoices, maintenance requests, announcements, and similar collections:

```text
Page header
  +-- Screen title
  +-- Optional primary add/create action
Search/filter area
  +-- Search input
  +-- Optional compact filters
Content state
  +-- Loading state
  +-- Empty state
  +-- Error state
  +-- List of white rows/cards
```

Rules:

- The title identifies the collection, for example `Quản lý người dùng`.
- The create action is visually prominent but must not crowd the title.
- Search fields should state what can be searched, for example `Tìm theo họ tên, số điện thoại, CCCD`.
- List rows must expose the primary identifier first, then one or two useful supporting values.
- Do not put every available field into the row. Put the full record in the detail screen.
- Tapping a row must have a clear visual affordance or a predictable whole-row interaction.

#### Detail Screen

Use this structure for a user, room, contract, invoice, or maintenance record:

```text
Back + title
Summary card
  +-- Identity or primary status
Information cards
  +-- Group 1: core information
  +-- Group 2: related information
  +-- Group 3: history or notes
Management card
  +-- Editable controls, if any
Action area
  +-- Primary action
  +-- Secondary or destructive action
```

Rules:

- Show the record identity before secondary fields.
- Group fields by user meaning, not by database table or API response order.
- Keep editable controls near the action that applies them.
- Keep destructive actions below or separated from the primary action.
- Preserve scrolling and make the final action reachable without clipping.

#### Create/Edit Screen

Use this structure for forms:

```text
Back + title
Short instruction or context
Form sections
  +-- Label
  +-- Input/control
  +-- Optional helper or validation message
Sticky or final action area
  +-- Save/submit
```

Rules:

- Every input must have a visible label; hints do not replace labels.
- Required fields must be identifiable without relying only on an asterisk.
- Keep one logical concern per section.
- Do not clear valid user input when one field fails validation.
- Show validation close to the field that needs correction.
- Disable or guard submission while a request is actively being sent.

#### Dashboard/Home Screen

Use the home screen to answer “what needs attention?” rather than showing every metric.

```text
Greeting or context
Priority summary
  +-- Outstanding maintenance
  +-- Unpaid invoices
  +-- Expiring contracts
Primary shortcuts
Recent or urgent activity
```

Rules:

- Limit summary cards to decisions users can act on.
- Every metric should lead to a relevant list or detail screen.
- Do not use large decorative charts unless they answer a real management question.

### Alignment And Reading Order

- Keep labels and values aligned consistently within a section.
- Place the most important value nearest the section heading.
- Keep start edges aligned across cards and fields.
- Avoid mixing centered and start-aligned content in the same information group unless the centered item is an intentional empty state.
- Use the XML order to match the visual and screen-reader reading order.
- Place actions after the content they affect.

## Spacing And Dimensions

Use `dp` for dimensions and `sp` for text. Never use `px` in layouts.

Recommended baseline values:

| Use | Value |
| --- | --- |
| Screen padding | `16dp` |
| Section/card gap | `12dp` |
| Card padding | `16dp` |
| Small internal gap | `4dp` or `8dp` |
| Standard control height | At least `48dp` |
| Card corner radius | `12dp` |
| Main screen title | `24sp` |
| Section heading | `14sp` to `16sp` |
| Primary value | `16sp` to `18sp` |
| Supporting label | `13sp` to `14sp` |

Use `0dp` with weights only when proportional expansion is intentional. Prefer `wrap_content` for content-driven views and avoid empty spacer containers.

### Spacing Scale

Use a small spacing scale instead of inventing arbitrary values.

| Token | Value | Typical use |
| --- | --- | --- |
| `space_xs` | `4dp` | Icon-to-label or tightly related text |
| `space_sm` | `8dp` | Label-to-value, button groups, compact rows |
| `space_md` | `12dp` | Card-to-card and section separation |
| `space_lg` | `16dp` | Screen padding and card padding |
| `space_xl` | `24dp` | Major section separation or empty-state breathing room |
| `space_xxl` | `32dp` | Rare, for prominent empty states or header separation |

When the same spacing appears in three or more resources, consider defining it in `dimens.xml`. Until then, a direct `dp` value is acceptable if it follows this scale.

### Size Rules

- Do not use fixed heights for text-heavy cards.
- Use `minHeight` rather than fixed `height` for buttons and rows when content may vary.
- Use fixed dimensions for icons and avatars only.
- Keep an icon's visible size smaller than its touch target; for example, a `24dp` icon inside a `48dp` button.
- Avoid horizontal layouts where long Vietnamese text and a fixed button compete for the same narrow width.
- Let body content wrap instead of truncating important values.

## Typography

Typography should establish hierarchy without excessive font variation.

- Screen titles use `dark_navy`, usually `24sp`, and bold weight.
- Section headings use `primary`, usually `14sp` to `16sp`, and bold weight.
- Field labels use `secondary`, usually `13sp` to `14sp`.
- Field values use `dark_navy`, usually `16sp`.
- Supporting metadata may use `secondary` at `13sp` to `14sp`.
- Allow long values to wrap naturally.
- Avoid all-caps for user-entered content; all-caps is acceptable for short section labels.
- Vietnamese text must remain natural and use string resources for reusable UI copy.

### Type Hierarchy

| Level | Default size | Weight | Color | Purpose |
| --- | --- | --- | --- | --- |
| Screen title | `24sp` | Bold | `dark_navy` | Names the screen |
| Primary identity | `18sp` | Bold | `dark_navy` | User, room, or record name |
| Section title | `14sp`-`16sp` | Bold | `primary` | Names a grouped content area |
| Field value | `16sp` | Normal or medium | `dark_navy` | Main readable data |
| Field label | `13sp`-`14sp` | Normal | `secondary` | Explains a value or control |
| Supporting text | `13sp`-`14sp` | Normal | `secondary` | Metadata, helper, timestamp |
| Button label | Native component default | Medium/Bold | Contrast-safe | Describes an action |

Do not use size alone to create hierarchy. Combine size with spacing, weight, and grouping.

### Content Formatting

- Use sentence case for normal Vietnamese labels.
- Use short uppercase labels only for compact section headings when they improve scanning.
- Use `Họ và tên`, `Số điện thoại`, `Ngày sinh`, `Địa chỉ`, and similar domain terms consistently.
- Keep button labels as verbs or clear outcomes: `Cập nhật`, `Lưu`, `Thêm người dùng`, `Xóa người dùng`.
- Avoid vague labels such as `OK`, `Xử lý`, or `Thực hiện` when a more specific action is available.
- Keep error messages actionable: explain what failed and what the user can do next.
- Use `Chưa cập nhật` for a missing profile value when the field exists but has no value.
- Use an empty-state explanation when an entire collection has no records; do not repeat `Chưa cập nhật` for a missing collection.

### String Resource Policy

New reusable or user-facing text should be placed in `res/values/strings.xml` rather than hard-coded in layout XML. Existing hard-coded text may be migrated when a screen is substantially redesigned, but presentation changes should not silently alter API values or enum keys.

## Cards And Surfaces

Use white rounded cards to group related content on the gray page background.

Cards should:

- Use `@color/white` as the fill.
- Use a subtle named-resource border when separation is needed.
- Use consistent `12dp` corner radius.
- Use `16dp` internal padding.
- Avoid heavy shadows and unnecessary elevation.
- Contain related information only; do not put every individual field in a separate card.

Reusable backgrounds belong in `res/drawable` and should reference named color resources.

### Surface Hierarchy

Use no more than three surface levels on a normal screen:

1. `background` for the page.
2. `white` for cards and primary content.
3. A named control or selected-state surface only when the interaction requires it.

Do not stack cards inside cards unless the inner card represents an independent object or clearly interactive item. Excessive nesting makes the screen feel fragmented.

### Borders And Elevation

- Prefer a subtle border over a strong shadow for ordinary information cards.
- Use elevation sparingly for interactive rows or floating controls.
- Keep border color and radius consistent across a screen.
- Avoid decorative gradients, glass effects, and heavy shadows unless a future design decision explicitly introduces them.

## Headers And Navigation

Every secondary screen should make its navigation context obvious.

- Keep the title short and descriptive.
- Use a compact back control with a minimum touch target of `48dp`.
- Provide `contentDescription` for icon-only controls.
- Keep the back action visually secondary to the screen title.
- Do not add a custom navigation pattern when the existing Navigation component already handles the flow.

### Header Variants

Use the smallest header that communicates context:

- **Standard secondary header:** back control plus title.
- **List header:** title plus optional create action.
- **Detail header:** back control plus title; record identity belongs in the summary card.
- **Modal/dialog header:** short title and explicit close/cancel action.

Do not duplicate the same title in both the header and the first card unless the first card contains a user or record name that is intentionally different from the screen title.

## Forms And Controls

Controls must reflect whether information is editable.

- Use labels that explain the expected value.
- Keep editable controls grouped with the action that saves them.
- Use disabled controls only for display-only state; do not make disabled controls look editable.
- Use native `RadioGroup`, `RadioButton`, `Spinner`, `EditText`, and `Button` patterns already used by the app before introducing custom components.
- Keep primary actions full width when they submit a screen-level operation.
- Keep destructive actions visually distinct but restrained; use a named palette resource.
- Do not hide important actions behind unexplained icons.

### Input Behavior

- Use the correct `inputType` for phone numbers, email addresses, dates, numbers, and free text.
- Use single-line input for short values and multi-line input for notes or addresses.
- Keep focus behavior predictable when validation fails.
- Preserve entered values through rotation or transient request failures when the surrounding architecture supports it.
- Avoid using a disabled input to display a value when a styled `TextView` is more honest.
- Use native date or time controls where the platform pattern is already available.

### Selection Controls

- Use radio buttons when the user selects one option from a short visible set.
- Use a spinner or dropdown when the option list is longer or space is limited.
- Use checkboxes only for independent choices, not mutually exclusive states.
- Use disabled radio buttons only for read-only display, as with the current gender presentation.
- Make the group label explain what the selection controls.

### Validation

Validation should distinguish between:

- **Required:** The user must provide a value.
- **Invalid:** The value exists but has an unacceptable format or range.
- **Unavailable:** The value cannot currently be loaded or selected.
- **Conflict:** The requested operation conflicts with existing data or state.

Each validation message should:

- Identify the affected field or operation.
- Use plain Vietnamese.
- Avoid blame or technical stack traces.
- Remain visible long enough to be read.
- Preserve valid values elsewhere on the form.

## Buttons And Actions

Use one clear primary action per screen whenever possible.

- Primary action: `@color/primary` background with readable text.
- Secondary action: neutral or `@color/secondary` treatment.
- Destructive action: clearly labeled, separated from the primary action, and never triggered accidentally.
- Buttons should have at least `48dp` height.
- Button text should describe the result, such as `Cập nhật`, `Lưu`, or `Xóa người dùng`.
- Show confirmation before destructive operations when data can be lost.

### Action Priority

Use this priority order:

1. **Primary:** The expected next step, such as `Lưu` or `Cập nhật`.
2. **Secondary:** A useful alternative, such as `Hủy` or `Quay lại`.
3. **Destructive:** An operation that removes, disables, or irreversibly changes data.

Do not give two unrelated actions identical visual weight. If two actions are equally important, place them in clearly labeled sections rather than styling both as primary.

### Request Feedback

During an asynchronous action:

- Prevent accidental duplicate submissions.
- Keep the action label understandable; a progress indicator may be shown alongside it.
- Do not navigate away until the operation's result is known.
- On success, show a concise confirmation and update or close the affected screen as appropriate.
- On failure, keep the user on the screen and explain whether retrying is safe.

### Destructive Actions

For delete or irreversible actions:

- Use a specific label such as `Xóa người dùng` rather than `Xóa` when context could be unclear.
- Keep the action separated from save/update controls.
- Explain what will happen in the confirmation dialog.
- Do not use a destructive action as the default focused button.
- Display server conflict messages without pretending deletion succeeded.

## Data States

Every data-driven screen should account for these states:

```text
Loading --> Content
   |         |
   v         v
 Error    Empty content
```

- Loading: show a clear progress indicator without presenting stale controls as ready.
- Content: show grouped data with stable labels.
- Empty: explain what is empty and what the user can do next.
- Error: show a useful Vietnamese message and preserve a retry or back path where appropriate.
- Missing field: keep the label visible and use the existing `Chưa cập nhật` placeholder.
- Long content: wrap text and preserve scrolling; never clip silently.

### State Definitions

| State | Meaning | Required UI behavior |
| --- | --- | --- |
| Loading | Request is in progress | Show progress and avoid misleading stale actions |
| Content | Data is available | Show the normal hierarchy and actions |
| Empty | Request succeeded with no records | Explain why the area is empty and offer the next action |
| Partial | Record exists with missing fields | Keep structure and show field-level placeholders |
| Error | Request failed | Show a useful message and retry/back path |
| Conflict | Action is rejected by domain rules | Explain the reason and preserve current data |
| Offline/unavailable | Service cannot be reached | Avoid presenting the result as current; offer retry |

### Empty State Pattern

```text
Illustration or quiet icon, if useful
Short title: what is empty
One-sentence explanation
Primary next action, if one exists
```

Do not use a large illustration when a short explanation and action are enough.

### Error State Pattern

```text
Short title: what could not be completed
Actionable explanation
Retry or return action
```

Do not expose exception names, HTTP status codes, SQL errors, or stack traces to end users.

## Accessibility

Accessibility is part of the visual design, not a later enhancement.

- Give every meaningful image and icon an appropriate `contentDescription`.
- Use at least `48dp` touch targets.
- Maintain readable contrast between text and surfaces.
- Do not communicate state through color alone; include text, selection, or an icon.
- Keep labels close to the controls or values they describe.
- Preserve logical reading order in the XML hierarchy.
- Test with larger font settings and narrow device widths.

### Accessibility Checklist

- Icon-only buttons have `contentDescription`.
- Decorative images use an empty or appropriate non-announcing description.
- Buttons and controls have at least a `48dp` touch area.
- Text does not depend on a specific font scale to remain readable.
- Important actions are reachable by keyboard, switch access, or screen reader navigation where supported.
- The focus order follows the visual order.
- Radio groups and related controls have a nearby label.
- Error text is associated with the relevant field or operation.
- Disabled controls are not the only way to communicate a read-only value.
- Status text is present in addition to status color.

## Responsive Android Layouts

- Use `match_parent`, `wrap_content`, and `0dp` weights deliberately.
- Prefer flexible vertical layouts over fixed-height content containers.
- Use `fillViewport="true"` on detail `ScrollView`s when the screen should fill short viewports.
- Avoid fixed widths for text-heavy views.
- Keep action buttons reachable after scrolling.
- Test at minimum one small phone viewport and one larger viewport.

### Viewport Test Matrix

Every substantial screen should be checked at minimum in these conditions:

| Test | Expected result |
| --- | --- |
| Small phone width | No horizontal clipping; all content scrolls vertically |
| Larger phone width | Cards remain aligned; no arbitrary stretched gaps |
| Large font scale | Labels and values wrap without overlap |
| Long Vietnamese text | Text wraps without hiding actions or values |
| Empty response | Empty state is understandable and not visually broken |
| Request failure | Error state does not leave stale controls misleadingly enabled |
| Keyboard visible | Focused input and submit action remain reachable |
| Rotation/recreation where supported | Layout restores without losing required context |

### Avoiding Common Responsive Failures

- Do not use `layout_width="wrap_content"` for a long value next to a fixed control unless the parent can handle overflow.
- Do not use fixed-height cards around dynamic text.
- Do not assume a 16:9 device or a particular navigation-bar size.
- Do not put all content in one horizontal row on a phone.
- Do not rely on a preview-only sample image to validate runtime spacing.
- Do not use `tools:` attributes as runtime behavior.

## Resource And Code Conventions

- Put colors in `res/values/colors.xml`.
- Put reusable text in `res/values/strings.xml`.
- Put reusable dimensions in `res/values/dimens.xml` when a value appears across multiple screens.
- Put reusable component styles in `res/values/styles.xml`.
- Put reusable shapes and selectors in `res/drawable`.
- Preserve existing view IDs when changing a layout that is bound by Java code.
- Prefer the smallest resource change that solves the visual problem.
- Do not change API, navigation, or fragment behavior for a presentation-only task.

### Naming

Use descriptive, stable names:

- Layouts: `fragment_<feature>.xml`, `item_<feature>.xml`, `dialog_<purpose>.xml`.
- IDs: `<feature>_<role>`, such as `user_detail_update` or `user_search`.
- Colors: semantic names such as `primary`, `dark_navy`, or `status_error`, not names based on a single component.
- Drawables: `<feature>_<purpose>_background` or `<feature>_<purpose>_selector`.
- Styles: `FeatureComponent` or a clearly shared component name.

Do not rename an ID in a presentation-only change when Java or navigation code references it. If renaming is necessary, treat it as a behavior-safe migration and update every caller in the same change.

### XML Quality Rules

- Keep namespaces at the root only.
- Keep attributes grouped logically: ID, dimensions, layout position, appearance, content, accessibility, and tools metadata.
- Avoid empty containers.
- Avoid duplicated style attributes when a shared style is appropriate.
- Use `layout_marginStart` and `layout_marginEnd` rather than left/right for directional layout spacing.
- Use `textSize` with `sp`, never `dp`.
- Use `paddingStart` and `paddingEnd` where direction matters.
- Keep `tools:` preview attributes separate from runtime attributes.

## Definition Of Done For UI Changes

A UI change is ready when:

- The screen follows the hierarchy and palette defined here.
- No layout uses `px` dimensions or inline presentation hex colors.
- Existing bindings and actions still work.
- Complete, empty, loading, and error states remain understandable where applicable.
- Small and large viewport behavior has been checked.
- Icon-only controls have content descriptions.
- The Android module builds successfully.
- Any intentional deviation from this guide is documented in the relevant change design.

## Screen Review Checklist

Before considering a screen complete, review it in this order:

### Structure

- Is the screen purpose clear within the first few seconds?
- Is the primary information shown before secondary information?
- Are related fields grouped by user meaning?
- Is there a clear start, middle, and action area?
- Can the user reach the final action by scrolling?

### Visual Design

- Does the screen use the established palette?
- Are surfaces limited and intentional?
- Is spacing based on the project scale?
- Are title, heading, label, and value levels distinguishable?
- Are cards aligned and consistently padded?
- Are icons supporting comprehension rather than filling empty space?

### Interaction

- Is the primary action obvious?
- Are editable and read-only values distinguishable?
- Are destructive actions separated and confirmed?
- Are duplicate submissions prevented during requests?
- Are success, error, conflict, and empty states accounted for?

### Content

- Are labels natural Vietnamese and consistent with other screens?
- Do buttons describe their result?
- Are missing values represented consistently?
- Are error messages actionable and non-technical?
- Are long values safe to wrap?

### Accessibility And Responsive Behavior

- Are touch targets at least `48dp`?
- Are icon-only controls described?
- Does larger text remain readable?
- Does the layout work on a small viewport?
- Is status communicated by text as well as color?
- Does screen-reader reading order match the visual order?

### Engineering Verification

- Are all referenced IDs preserved or all callers updated?
- Are there no inline hex colors in the changed layout?
- Are there no `px` dimensions?
- Do resources compile?
- Does the Android module build?
- Has the screen been checked with populated, missing, empty, loading, and error data where applicable?

## Reference: User Detail Screen

The current user detail screen is the reference implementation for the detail pattern:

```text
@color/background page
  +-- 48dp back control + 24sp title
  +-- White profile summary card
  |     +-- Avatar
  |     +-- User identity and contact values
  +-- White personal information card
  |     +-- Date of birth
  |     +-- Read-only gender group
  +-- White address and identity card
  |     +-- Permanent address
  |     +-- Identity document fields
  +-- White emergency contact card
  |     +-- Contact name and phone
  |     +-- Additional note
  +-- White account management card
  |     +-- Editable role group
  |     +-- Editable status group
  +-- Primary update button
  +-- Separated delete button
```

When creating another detail screen, reuse this information hierarchy and adjust only the domain-specific sections. Do not copy user-specific labels into unrelated screens.

## Design Change Policy

This guide is the default, not a reason to block a legitimate product need. A feature may diverge when:

- The interaction is fundamentally different from a standard list, detail, or form screen.
- A platform component requires a different layout or state treatment.
- Accessibility or content clarity requires an exception.
- A product requirement explicitly introduces a new visual system.

When deviating, document:

1. Which rule is being changed.
2. Why the default pattern is insufficient.
3. What new behavior or visual rule replaces it.
4. Which screens should reuse the new pattern.
5. How the change was checked for accessibility and responsive behavior.
