# AI Usage Declaration

This document outlines how the **Antigravity AI Coding Assistant** was utilized to build this project.

### 1. Tools and Help Provided
- **Project Bootstrapping**: Handled downloading the Spring Initializr package with H2, Validation, Web, and JPA dependencies.
- **Code Generation**: Implemented the 3-color DFS topological sort algorithm, JPA mappings, REST controllers, custom exceptions, validation DTOs, and the Strategy pattern for task recommendation.
- **Test Generation**: Generated 26 unit and integration test cases covering happy path and edge cases (direct/indirect cycles, missing tasks, validation errors, and recommendation sorting).

### 2. Suggestion Rejected or Changed
- **Initial Suggestion**: The assistant initially proposed building a Spring Boot 4.x application containing the recently introduced Spring Boot 3.4.0 `@MockitoBean` annotation.
- **Rejection & Change**: This was rejected due to build failures since Spring Boot 3.4+ packages weren't fully resolved. The project was downgraded to Spring Boot **3.3.2** (stable) and migrated to use `@MockBean`, which successfully resolved the compilation errors.

### 3. Code Verification
- **Automated Testing**: Verified using `./mvnw clean test`, passing all 26 test cases.
- **Logging Verification**: Structured logs were validated via console output to verify logger class names, severities (`INFO`, `WARN`, `ERROR`), thread names, and message payloads.

### 4. Limitations
- None. The project implements all minimum requirements, engineering requirements, and includes OpenAPI integration and H2 persistent file storage.
