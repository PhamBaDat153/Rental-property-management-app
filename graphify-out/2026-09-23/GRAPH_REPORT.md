# Graph Report - Rental-property-management-app  (2026-09-23)

## Corpus Check
- Corpus is ~3,967 words - fits in a single context window. You may not need a graph.

## Summary
- 90 nodes · 111 edges · 12 communities (10 shown, 2 thin omitted)
- Extraction: 100% EXTRACTED · 0% INFERRED · 0% AMBIGUOUS
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Shared Domain Models
- Tenant Entity Lifecycle
- User Entity Lifecycle
- Maven Wrapper
- Role Entity Lifecycle
- Security Configuration
- Application Tests
- Spring Boot Startup
- Gender Enumeration
- Identity Types
- Project Documentation
- Backend Package

## God Nodes (most connected - your core abstractions)
1. `User` - 13 edges
2. `Tenant` - 12 edges
3. `Role` - 11 edges
4. `ActiveStatus` - 7 edges
5. `IdentityType` - 6 edges
6. `Gender` - 4 edges
7. `BeApplication` - 3 edges
8. `SecurityConfig` - 3 edges
9. `BeApplicationTests` - 3 edges
10. `ACTIVE` - 1 edges

## Surprising Connections (you probably didn't know these)
- `Role` --references--> `ActiveStatus`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/Role.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Enum/ActiveStatus.java
- `User` --references--> `ActiveStatus`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/User.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Enum/ActiveStatus.java
- `Tenant` --references--> `IdentityType`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/Tenant.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/Enum/IdentityType.java
- `User` --references--> `Role`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/User.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/Role.java
- `User` --references--> `Tenant`  [EXTRACTED]
  Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/User.java → Source/BE/src/main/java/rentalpropertymanagementapp/be/Model/User/Tenant.java

## Import Cycles
- None detected.

## Communities (12 total, 2 thin omitted)

### Community 0 - "Shared Domain Models"
Cohesion: 0.26
Nodes (9): generators, localdate, localdatetime, lombok, persistence, ActiveStatus, ACTIVE, INACTIVE (+1 more)

### Community 1 - "Tenant Entity Lifecycle"
Cohesion: 0.17
Nodes (10): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter (+2 more)

### Community 2 - "User Entity Lifecycle"
Cohesion: 0.17
Nodes (10): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, PreUpdate, Setter (+2 more)

### Community 3 - "Maven Wrapper"
Cohesion: 0.38
Nodes (8): mvnw script, clean(), die(), exec_maven(), hash_string(), set_java_home(), trim(), verbose()

### Community 4 - "Role Entity Lifecycle"
Cohesion: 0.20
Nodes (9): AllArgsConstructor, Builder, Entity, Getter, NoArgsConstructor, PrePersist, Setter, Table (+1 more)

### Community 5 - "Security Configuration"
Cohesion: 0.36
Nodes (6): bcryptpasswordencoder, org.springframework.context.annotation.Bean, org.springframework.security.config.annotation.web.builders.HttpSecurity, org.springframework.security.crypto.password.PasswordEncoder, org.springframework.security.web.SecurityFilterChain, SecurityConfig

### Community 6 - "Application Tests"
Cohesion: 0.60
Nodes (3): org.junit.jupiter.api.Test, org.springframework.boot.test.context.SpringBootTest, BeApplicationTests

### Community 7 - "Spring Boot Startup"
Cohesion: 0.50
Nodes (3): org.springframework.boot.autoconfigure.SpringBootApplication, BeApplication, springapplication

### Community 8 - "Gender Enumeration"
Cohesion: 0.40
Nodes (4): Gender, FEMALE, MALE, OTHER

### Community 9 - "Identity Types"
Cohesion: 0.40
Nodes (4): IdentityType, ID, OTHER, PASSPORT

## Knowledge Gaps
- **11 isolated node(s):** `RentalPropertyManagementApp:BE`, `ACTIVE`, `INACTIVE`, `MALE`, `FEMALE` (+6 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 45 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `User` connect `User Entity Lifecycle` to `Shared Domain Models`, `Tenant Entity Lifecycle`, `Role Entity Lifecycle`?**
  _High betweenness centrality (0.195) - this node is a cross-community bridge._
- **Why does `Tenant` connect `Tenant Entity Lifecycle` to `Shared Domain Models`, `Identity Types`, `User Entity Lifecycle`?**
  _High betweenness centrality (0.167) - this node is a cross-community bridge._
- **Why does `Role` connect `Role Entity Lifecycle` to `Shared Domain Models`, `User Entity Lifecycle`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **What connects `RentalPropertyManagementApp:BE`, `ACTIVE`, `INACTIVE` to the rest of the system?**
  _11 weakly-connected nodes found - possible documentation gaps or missing edges._