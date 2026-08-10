# Overt Ideas and Solutions - Software Developer Technical Assessment

A clean, production-ready implementation of the technical assessment using **Java 17** and **Spring Boot 3.3.2**.

---

## Project Structure

This project follows standard corporate packaging patterns, adhering strictly to SOLID design principles, clean code practices (DRY, meaningful self-documenting naming conventions, and constants separation), and comprehensive automated testing.

- **`part_a/` (Dependency Planner)**:
  - Isolated dependency-free library component that plans execution orders based on topological sorting.
- **`part_b/` (Smart Task Queue API)**:
  - Layered REST API using Spring Web, Spring Data JPA, and local H2 file storage.
  - Implements the **Strategy Pattern** for the task recommendation engine.
  - Exception handling using Spring AOP (`@ControllerAdvice`).
  - API documentation using Springdoc OpenAPI / Swagger UI.
  - Structured console logging using Logback configuration.

---

## Rationale & Complexity Analysis

### Part A - Topological Sort Complexity
We implemented topological sorting using a depth-first search (DFS) with three-color node states (`UNVISITED`, `VISITING`, `VISITED`):
- **Time Complexity**: $\mathcal{O}(V + E)$, where $V$ is the number of tasks and $E$ is the number of dependencies. Each task is visited at most once, and each dependency edge is scanned once.
- **Space Complexity**: $\mathcal{O}(V + E)$ to store the adjacency map representation of the graph, the recursion stack, and the node state mapping.

### Part B - Deterministic Recommendation Engine
The recommendation engine sorts pending tasks by applying a strict, deterministic comparator:
1. **Priority**: `CRITICAL` (weight 4) > `HIGH` (weight 3) > `MEDIUM` (weight 2) > `LOW` (weight 1).
2. **Due Date**: Tasks with closer/earlier due dates are prioritized. Tasks without due dates are pushed to the end.
3. **Creation Time**: Oldest created tasks first (FIFO tie-breaker).

Sorting complexity is $\mathcal{O}(N \log N)$ where $N$ is the number of pending tasks.

---

## Setup and Running Instructions

### Prerequisites
- JDK 17 must be installed and configured on your system (managed automatically if run via the provided scripts).

### 1. Compile and Run Tests
To run all 26 automated unit and integration tests covering the topological sorting edge cases and REST endpoints:
```bash
./mvnw clean test
```

### 2. Run the Spring Boot Server
To start the REST API on port `8080`:
```bash
./mvnw spring-boot:run
```

### 3. Verify Endpoints & Interactive API Docs
Once the server starts:
- **Swagger UI**: Access the interactive API docs at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) to try out endpoints directly.
- **H2 Database Console**: Access the database console at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/tasksdb`, Username: `sa`, Password: `password`).
