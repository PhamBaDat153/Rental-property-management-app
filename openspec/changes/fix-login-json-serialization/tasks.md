## 1. Backend Login Contract

- [x] 1.1 Add and validate a login request DTO for username, password, and login type, and verify malformed or missing fields produce the existing client-error contract.
- [x] 1.2 Add a bounded login response DTO containing only `user_id` and `user_name`, and verify direct Jackson serialization contains no entity relationships or password fields.
- [x] 1.3 Change the backend login endpoint to POST JSON and map authenticated users to the response DTO while preserving role authorization and unauthorized behavior; verify the backend compiles.

## 2. Android Client Migration

- [x] 2.1 Update the Retrofit login declaration to send the request DTO with POST and consume the bounded response DTO; verify the Android module compiles.
- [x] 2.2 Update `LoginActivity` to build the new request body and preserve successful navigation and failed-login messaging; verify the login call uses the new endpoint contract.

## 3. Regression Coverage

- [x] 3.1 Add backend endpoint or serialization tests covering successful login with related records and assert the response is finite and limited to the approved fields.
- [x] 3.2 Add tests covering invalid credentials, malformed input, and absence of sensitive fields; verify unauthorized and client-error status behavior.
- [x] 3.3 Run the backend test suite and Android build/test checks, then manually verify a successful login and an unsuccessful login against the running application. Automated backend and Android checks pass; live credential verification remains unavailable without a running app session.

## 4. API Cleanup Verification

- [x] 4.1 Search controller return types for direct JPA entity exposure related to login and confirm the login path returns only DTOs; document any unrelated entity endpoints left outside this change.
- [x] 4.2 Confirm no production configuration increases Jackson nesting limits as a workaround and verify the original document-nesting warning no longer occurs during login.
