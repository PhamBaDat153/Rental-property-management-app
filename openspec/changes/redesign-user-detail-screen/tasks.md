## 1. Layout Structure

- [x] 1.1 Reorganize `fragment_user_detail.xml` into a scrollable header, profile summary, personal-information card, address/identity card, emergency-contact card, management card, and action area; verify all existing fragment-referenced view IDs remain present.
- [x] 1.2 Remove empty spacer containers and replace all `px` dimensions and `dp` text sizes with density-independent `dp` dimensions and scalable `sp` text sizes; verify the layout contains no presentation-critical `px` values.
- [x] 1.3 Preserve the existing `TextView`, `RadioGroup`, `RadioButton`, and button IDs/types used by `UserDetailFragment`; verify the Android resource compiler and layout inflation succeed.

## 2. Visual Styling

- [x] 2.1 Apply the existing `background`, `primary`, `secondary`, `dark_navy`, and `white` resources to page, card, heading, label, value, and primary-action states; verify no inline hard-coded presentation color remains in the redesigned layout.
- [x] 2.2 Add or update only the required rounded card/action drawable or style resources, using named palette colors and consistent corner/spacing treatment; verify the resources compile and render without missing references.
- [x] 2.3 Make the header, section labels, profile values, disabled gender display, editable role/status controls, update action, and delete action visually distinct and touch-friendly; verify the complete and incomplete profile states remain readable.

## 3. Verification

- [x] 3.1 Build the Android app with the project's Gradle wrapper and verify the redesigned resources compile without errors.
- [ ] 3.2 Exercise the detail screen with populated and null profile fields and verify labels, `Chưa cập nhật` placeholders, gender, role, and status controls display correctly.
- [ ] 3.3 Verify back navigation, role/status update, delete action, vertical scrolling on a small viewport, and reachability of the bottom actions without changing Java/API behavior.
