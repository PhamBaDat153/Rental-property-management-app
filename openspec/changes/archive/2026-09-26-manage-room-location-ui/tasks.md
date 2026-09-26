## 1. API Models And Navigation Foundation

- [x] 1.1 Add typed Android Location and Room response/request models matching the existing DTO fields, nullable values, enum statuses, numeric values, and image URL list; verify the app module compiles.
- [x] 1.2 Extend `ApiService` with Location list/detail/create/update/delete and Room list/detail/create/update/delete methods, including the Room multipart `room` JSON part and optional `images` parts; verify Retrofit declarations compile and endpoint paths match `/be/locations` and `/be/rooms`.
- [x] 1.3 Replace the `propertiesFragment` placeholder destination with the properties management fragment and add any detail destinations/arguments required for back navigation; verify bottom navigation opens the new screen.

## 2. Properties List UI

- [x] 2.1 Create the properties layout using the existing background, card, color, spacing, and native-control patterns with separate Location and Room views; verify the layout renders on a small Android viewport without clipped primary actions.
- [x] 2.2 Add Location and Room row layouts/adapters showing the required primary and supporting values, including `Chưa đặt tên phòng` for blank room names and separate room-code metadata; verify representative populated and blank-name records render correctly.
- [x] 2.3 Implement Location loading, local filtering by code or all address components, and loading/empty/error states; verify typing and clearing the filter update only the visible Location rows.
- [x] 2.4 Implement Room loading, local filtering by non-blank `room_name` only, and loading/empty/error states; verify room codes do not cause unnamed rooms to match arbitrary filter text.
- [x] 2.5 Add list row click handling to open the correct Location or Room detail flow and preserve the selected resource identifier; verify back navigation returns to the appropriate list state.

## 3. Location CRUD UI

- [x] 3.1 Implement Location create and edit forms with visible labels, required-field validation, status selection, address fields, description, and submit-state protection; verify invalid input remains visible and valid input is sent only after validation passes.
- [x] 3.2 Implement Location detail loading and grouped display for code, address, status, and description; verify a successful detail response populates every supported field and a failed response remains recoverable.
- [x] 3.3 Implement Location update success handling and list/detail refresh; verify a changed field is visible after a successful PUT response.
- [x] 3.4 Implement confirmed Location deletion with success navigation and not-found/conflict/error feedback; verify a location referenced by rooms is not removed optimistically after a conflict response.

## 4. Room CRUD UI

- [x] 4.1 Implement Room create and edit forms with Location selection, room code/name, floor, area, maximum occupants, rent, status, and description validation; verify required and non-negative/positive constraints are enforced before submission.
- [x] 4.2 Encode Room create/update requests as the backend-compatible multipart payload and handle optional image parts without adding an image-gallery scope; verify the generated request contains the JSON `room` part and succeeds against the existing endpoint contract.
- [x] 4.3 Implement Room detail loading and grouped display for identity, Location, room measurements/capacity, pricing, status, description, and available image URLs where supported; verify blank names use the specified display text.
- [x] 4.4 Implement Room update success handling and list/detail refresh; verify changed room data is visible after a successful PUT response.
- [x] 4.5 Implement confirmed Room deletion with success navigation and not-found/conflict/error feedback; verify a room referenced by a rental contract remains visible after a conflict response.

## 5. Verification And Polish

- [x] 5.1 Add focused tests or pure filter checks for Location code/address matching, Room-name-only matching, and blank room-name presentation; verify case-insensitive behavior and clearing filters.
- [x] 5.2 Run the Android build/test command from `Source/Rentaly_Management` and fix compilation, resource, navigation, or Retrofit errors.
- [ ] 5.3 Manually exercise properties navigation, both list views, create/edit/detail/delete flows, validation, empty/loading/error states, and backend conflict responses; verify Vietnamese labels and actions remain accessible on small Android screens.
