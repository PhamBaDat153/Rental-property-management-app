## Context

The backend currently authenticates through `UserServiceImplement.authenticate` and returns the JPA `User` entity directly from `UserController`. The Android client consumes only `user_id` and `user_name`, while the entity exposes bidirectional relationships such as tenant, announcements, and maintenance requests. The Android app uses Retrofit and the backend uses Spring MVC with Jackson.

## Goals / Non-Goals

**Goals:**

- Make the login response finite and limited to the Android client's required fields.
- Move credentials out of the URL and into a JSON POST request body.
- Preserve current password verification, role authorization, and unauthorized behavior.
- Add regression coverage for response shape and recursive serialization.

**Non-Goals:**

- Changing password hashing, role rules, session/token behavior, or database mappings.
- Refactoring every existing endpoint in this change.
- Supporting a second login response shape indefinitely.

## Decisions

### Use dedicated request and response DTOs

Add a login request DTO for username, password, and login type, plus a response DTO containing only `user_id` and `user_name`. The controller maps the authenticated entity to the response DTO before Jackson serialization. This is safer than increasing Jackson's nesting limit or adding relationship annotations until the current graph happens to serialize.

### Change login from GET to POST

Use `POST /be/user/login` with a JSON body. Passwords in query parameters can appear in access logs, proxy logs, browser history, and monitoring systems. The Android Retrofit declaration and `LoginActivity` will be updated together so the API contract changes atomically.

### Keep entity serialization defensive, but do not use it as the API contract

Existing inverse-side serialization protection may remain for defensive use, but login correctness will depend on the DTO boundary. New REST responses should not return JPA entities directly. A global Jackson nesting-depth increase is rejected because it hides graph design problems and still permits excessive payloads.

### Test at both controller and serialization boundaries

Add focused backend tests for successful and failed login responses and verify that the serialized success body contains only the bounded fields. Update or add Android-side request-contract coverage if the existing test setup supports it.

## Risks / Trade-offs

- [Existing clients call the GET endpoint] -> Update the checked-in Android client in the same change; document the endpoint as a breaking API change and avoid preserving a password-bearing GET compatibility path.
- [Some clients expect additional user fields] -> The current Android client reads only the identifier and username; confirm other consumers before implementation and add fields only if they are explicitly required.
- [Old clients fail after deployment] -> Deploy backend and Android changes together, or temporarily coordinate rollout at the API gateway if independent deployment is required.
- [Tests depend on database-backed authentication] -> Keep the serialization test independent of the database and use the existing backend test conventions for endpoint behavior.

## Migration Plan

1. Add request and response contracts and backend tests.
2. Switch the Android client to POST with the JSON request body.
3. Deploy backend and client changes together.
4. Verify successful login, invalid login, and response field boundaries.
5. Roll back by restoring the previous backend/client pair if coordinated deployment is not possible; do not restore a password-bearing GET endpoint as a long-term design.
