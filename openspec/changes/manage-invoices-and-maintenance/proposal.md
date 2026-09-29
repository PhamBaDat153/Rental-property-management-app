## Why

The Android utilities area currently exposes service catalog and room-meter tools, but invoices and maintenance requests have no usable API or Android workflow. Landlords therefore cannot review billing state or manage repair work from the application despite the backend domain models already existing.

## What Changes

- Add CRUD APIs for invoices and invoice items, including contract references, totals, due dates, status, notes, and line items.
- Add CRUD APIs for maintenance requests, including room/user references, title, description, priority, status, completion state, and request images where supported by the existing model.
- Add Android DTOs, Retrofit methods, screens, layouts, loading/empty/error states, and navigation for invoices and maintenance.
- Replace the Meter shortcut in `ServiceFragment` with Invoice and Maintenance shortcuts; keep meter management accessible from room detail.
- Add create, update, detail, and delete interactions with validation and confirmation for destructive operations.

## Capabilities

### New Capabilities

- `invoice-crud`: Manage invoices and invoice items through backend APIs and Android UI.
- `maintenance-crud`: Manage maintenance requests through backend APIs and Android UI.

### Modified Capabilities

- `rental-resource-ui`: Replace the utilities navigation shortcut for meters with invoice and maintenance entry points while retaining service catalog access.

## Impact

- Backend: invoice and maintenance controllers, DTOs, repositories, application services, validation, relationship checks, and tests.
- Android: Retrofit API, DTOs, navigation graph, `ServiceFragment`, invoice/maintenance fragments and XML layouts.
- Existing invoice, meter-reading, contract, room, user, and service relationships must remain intact.
- No database schema migration is expected; implementation uses the existing invoice, invoice-item, maintenance-request, and request-image tables.
