# Graph Report - Rental-property-management-app  (2026-09-23)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 398 nodes · 623 edges · 29 communities (24 shown, 5 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS · INFERRED: 2 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `f6f7e706`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- User
- lombok
- MaintenanceRequest
- RentalContract
- RoomService
- UserAnnouncementId
- MeterReading
- Room
- Announcement
- Invoice
- UserAnnouncement
- Tenant
- ExampleInstrumentedTest.java
- Meter
- Location
- MainActivity.java
- SecurityConfig.java
- mvnw
- BeApplicationTests.java
- BeApplication.java
- Gender
- IdentityType
- ActiveStatus
- gradlew
- README.md
- RentalPropertyManagementApp:BE

## God Nodes (most connected - your core abstractions)
1. `User` - 25 edges
2. `Room` - 21 edges
3. `Role` - 19 edges
4. `Tenant` - 17 edges
5. `MaintenanceRequest` - 16 edges
6. `RentalContract` - 16 edges
7. `UserAnnouncement` - 14 edges
8. `Meter` - 14 edges
9. `ContractTenant` - 14 edges
10. `RoomService` - 14 edges

## Surprising Connections (you probably didn't know these)
- `UserAnnouncement` --references--> `User`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Announcement/UserAnnouncement.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/User.java
- `MaintenanceRequest` --references--> `User`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Maintenance/MaintenanceRequest.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/User.java
- `Tenant` --references--> `User`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/Tenant.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/User.java
- `TenantRepository` --references--> `Tenant`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Repository/TenantRepository.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/Tenant.java
- `Meter` --references--> `AvailableStatus`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Meter/Meter.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Enum/AvailableStatus.java

## Import Cycles
- None detected.

## Communities (29 total, 5 thin omitted)

### Community 0 - "User"
Cohesion: 0.06
Nodes (41): autowired, controller, list, optional, org.springframework.data.jpa.repository.JpaRepository, org.springframework.http.ResponseEntity, org.springframework.stereotype.Service, org.springframework.web.bind.annotation.GetMapping (+33 more)

### Community 1 - "lombok"
Cohesion: 0.25
Nodes (13): bigdecimal, generators, linkedhashset, localdate, localdatetime, lombok, persistence, rentalpropertymanagementapp.be.Model.Enum.ActiveStatus (+5 more)

### Community 2 - "MaintenanceRequest"
Cohesion: 0.07
Nodes (23): MaintenanceStatus, COMPLETED, PENDING, PROCESSING, AllArgsConstructor, Builder, Entity, Getter (+15 more)

### Community 3 - "RentalContract"
Cohesion: 0.08
Nodes (20): ContractTenant, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate (+12 more)

### Community 4 - "RoomService"
Cohesion: 0.08
Nodes (20): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter (+12 more)

### Community 5 - "UserAnnouncementId"
Cohesion: 0.11
Nodes (20): jakarta.persistence.Embeddable, serializable, AllArgsConstructor, EqualsAndHashCode, Getter, NoArgsConstructor, Setter, UserAnnouncementId (+12 more)

### Community 6 - "MeterReading"
Cohesion: 0.09
Nodes (19): InvoiceItem, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, Setter (+11 more)

### Community 7 - "Room"
Cohesion: 0.09
Nodes (19): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter (+11 more)

### Community 8 - "Announcement"
Cohesion: 0.12
Nodes (13): Announcement, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate (+5 more)

### Community 9 - "Invoice"
Cohesion: 0.12
Nodes (13): InvoiceStatus, RECEIVED, SENTED, Invoice, AllArgsConstructor, Builder, Entity, Getter (+5 more)

### Community 10 - "UserAnnouncement"
Cohesion: 0.14
Nodes (12): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, Setter, Table (+4 more)

### Community 11 - "Tenant"
Cohesion: 0.14
Nodes (12): rentalpropertymanagementapp.be.Model.Enum.Gender, rentalpropertymanagementapp.be.Model.Enum.IdentityType, AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist (+4 more)

### Community 12 - "ExampleInstrumentedTest.java"
Cohesion: 0.24
Nodes (8): androidx.test.ext.junit.runners.AndroidJUnit4, assert, context, instrumentationregistry, org.junit.runner.RunWith, org.junit.Test, ExampleInstrumentedTest, ExampleUnitTest

### Community 13 - "Meter"
Cohesion: 0.17
Nodes (10): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter (+2 more)

### Community 14 - "Location"
Cohesion: 0.17
Nodes (10): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter (+2 more)

### Community 15 - "MainActivity.java"
Cohesion: 0.24
Nodes (8): android.os.Bundle, androidx.appcompat.app.AppCompatActivity, edgetoedge, insets, Override, MainActivity, viewcompat, windowinsetscompat

### Community 16 - "SecurityConfig.java"
Cohesion: 0.33
Nodes (7): bcryptpasswordencoder, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.security.config.annotation.web.builders.HttpSecurity, org.springframework.security.crypto.password.PasswordEncoder, org.springframework.security.web.SecurityFilterChain, SecurityConfig

### Community 17 - "mvnw"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 18 - "BeApplicationTests.java"
Cohesion: 0.60
Nodes (3): org.junit.jupiter.api.Test, org.springframework.boot.test.context.SpringBootTest, BeApplicationTests

### Community 19 - "BeApplication.java"
Cohesion: 0.50
Nodes (3): org.springframework.boot.autoconfigure.SpringBootApplication, BeApplication, springapplication

### Community 20 - "Gender"
Cohesion: 0.40
Nodes (4): Gender, FEMALE, MALE, OTHER

### Community 21 - "IdentityType"
Cohesion: 0.40
Nodes (4): IdentityType, ID, OTHER, PASSPORT

### Community 22 - "ActiveStatus"
Cohesion: 0.50
Nodes (3): ActiveStatus, ACTIVE, INACTIVE

### Community 23 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

## Knowledge Gaps
- **22 isolated node(s):** `AVAILABLE`, `UNAVAILABLE`, `NOT_READ`, `READ`, `COMPLETED` (+17 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 221 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **5 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `User` connect `User` to `Tenant`, `lombok`, `UserAnnouncement`, `MaintenanceRequest`?**
  _High betweenness centrality (0.152) - this node is a cross-community bridge._
- **Why does `Room` connect `Room` to `lombok`, `MaintenanceRequest`, `RentalContract`, `RoomService`, `Meter`, `Location`?**
  _High betweenness centrality (0.129) - this node is a cross-community bridge._
- **Why does `MaintenanceRequest` connect `MaintenanceRequest` to `User`, `lombok`, `Room`?**
  _High betweenness centrality (0.096) - this node is a cross-community bridge._
- **What connects `AVAILABLE`, `UNAVAILABLE`, `NOT_READ` to the rest of the system?**
  _22 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `User` be split into smaller, more focused modules?**
  _Cohesion score 0.05888376856118792 - nodes in this community are weakly interconnected._
- **Should `MaintenanceRequest` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `RentalContract` be split into smaller, more focused modules?**
  _Cohesion score 0.08333333333333333 - nodes in this community are weakly interconnected._