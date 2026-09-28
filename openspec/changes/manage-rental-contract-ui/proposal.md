## Why

The Android app exposes a contracts navigation entry, but it currently renders a placeholder instead of a usable RentalContract workflow. The backend already provides contract CRUD with multipart document upload, so landlords need a consistent Rentaly list, form, and detail experience for managing contracts, finding contracts by room code, and accessing their PDF documents.

## What Changes

- Replace the contracts placeholder screen with a RentalContract list following the established Rentaly list pattern.
- Load contracts and rooms on Android, associate records by `room_id`, and filter contracts by the related `room_code` without changing the backend response DTO.
- Add create and edit forms for room, dates, financial terms, payment settings, status, and contract terms.
- Allow selecting one PDF document while creating or editing a contract and send it through the existing multipart API.
- Add a dedicated contract detail screen with grouped read-only information, edit and confirmed delete actions, and document actions.
- Open an existing document with the device PDF application or browser and offer native Android download handling.
- Represent loading, empty, missing-room, validation, conflict, upload, and request-error states with Vietnamese labels consistent with `UI_UX_DESIGN.md`.

## Capabilities

### New Capabilities

- `rental-contract-ui`: Android list, filter, create/edit, detail, CRUD, PDF upload, preview handoff, and download behavior for rental contracts.

### Modified Capabilities

- `rental-resource-ui`: Extend the resource-management navigation and UI behavior with the contract collection and room-code filtering workflow.

## Impact

- Android Java/XML code under `Source/Rentaly_Management/app`, including the contracts fragment, DTOs, Retrofit service methods, form/detail/list layouts, navigation destinations, and native document handling.
- Existing `/be/contracts` multipart CRUD endpoints and `/be/rooms` list endpoint are consumed without a backend schema or response change.
- Existing Room and Rentaly UI patterns, colors, dimensions, Vietnamese strings, and data-state rules in `UI_UX_DESIGN.md` remain the visual baseline.
- No new PDF viewer dependency is required; viewing is delegated to installed Android PDF/browser applications and downloading uses the platform `DownloadManager`.
