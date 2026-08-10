# Overt Ideas and Solutions - Software Developer Technical Assessment

A clean, production-ready implementation of the technical assessment using **Java 17** and **Spring Boot 3.3.2**.

---

## Architectural Design (Before & After Factory Integration)

### 1. High-Level Architecture (HLD)

#### Version A: Current System (Without Factory Pattern)
```mermaid
graph TD
    Client[HTTP Client / Swagger UI] -->|REST API Requests| TaskController[TaskController]
    TaskController -->|CRUD operations & recommendation triggers| TaskService[TaskService]
    TaskService -->|Persists tasks| TaskRepository[TaskRepository]
    TaskRepository -->|Executes SQL| DB[(H2 Persistent File DB)]
    
    subgraph Recommendation Engine
        TaskService -->|Queries best task| TaskRecommendationStrategy[TaskRecommendationStrategy Interface]
        TaskRecommendationStrategy -->|Default logic| DefaultStrategy[DefaultRecommendationStrategy]
    end

    subgraph Topological Sort Library
        Solution[Solution Class] -->|Invokes planning| DependencyPlanner[DependencyPlanner]
    end
```

#### Version B: Scaled System (With Strategy Factory Integration)
```mermaid
graph TD
    Client[HTTP Client / Swagger UI] -->|REST API Requests| TaskController[TaskController]
    TaskController -->|CRUD operations & recommendation triggers| TaskService[TaskService]
    TaskService -->|Persists tasks| TaskRepository[TaskRepository]
    TaskRepository -->|Executes SQL| DB[(H2 Persistent File DB)]
    
    subgraph Dynamic Recommendation Engine
        TaskService -->|Requests strategy by name| StrategyFactory[TaskRecommendationStrategyFactory]
        StrategyFactory -->|Resolves strategy| TaskRecommendationStrategy[TaskRecommendationStrategy Interface]
        TaskRecommendationStrategy -->|Default logic| DefaultStrategy[DefaultRecommendationStrategy]
        TaskRecommendationStrategy -->|Alternative logic| AlternativeStrategy[SjfRecommendationStrategy]
    end

    subgraph Topological Sort Library
        Solution[Solution Class] -->|Invokes planning| DependencyPlanner[DependencyPlanner]
    end
```

---

### 2. Low-Level Design (LLD)

#### Version A: Current System (Without Factory Pattern)
```mermaid
classDiagram
    class TaskController {
        -TaskService taskService
        +createTask(TaskRequestDto) ResponseEntity
        +listTasks(TaskStatus, Pageable) ResponseEntity
        +updateTaskStatus(String, TaskStatus) ResponseEntity
        +deleteTask(String) ResponseEntity
        +getNextRecommendedPendingTask() ResponseEntity
    }

    class TaskService {
        -TaskRepository taskRepository
        -TaskRecommendationStrategy recommendationStrategy
        +createTask(TaskRequestDto) TaskResponseDto
        +listTasks(TaskStatus, Pageable) Page~TaskResponseDto~
        +updateTaskStatus(String, TaskStatus) TaskResponseDto
        +deleteTask(String) void
        +recommendNextPendingTask() TaskResponseDto
    }

    class TaskRecommendationStrategy {
        <<interface>>
        +recommendNextTask(List~Task~) Optional~Task~
    }

    class DefaultRecommendationStrategy {
        +recommendNextTask(List~Task~) Optional~Task~
        -getPriorityWeight(TaskPriority) int
    }

    class DependencyPlanner {
        +planExecutionOrder(List~TaskNode~) List~String~
        -findTopoSort(int, int[], int[], ArrayList, Stack, List) void
    }

    TaskController --> TaskService : uses
    TaskService --> TaskRecommendationStrategy : delegates selection
    DefaultRecommendationStrategy ..|> TaskRecommendationStrategy : implements
    TaskService --> TaskRepository : uses
```

#### Version B: Scaled System (With Strategy Factory Integration)
```mermaid
classDiagram
    class TaskController {
        -TaskService taskService
        +createTask(TaskRequestDto) ResponseEntity
        +listTasks(TaskStatus, Pageable) ResponseEntity
        +updateTaskStatus(String, TaskStatus) ResponseEntity
        +deleteTask(String) ResponseEntity
        +getNextRecommendedPendingTask(String) ResponseEntity
    }

    class TaskService {
        -TaskRepository taskRepository
        -TaskRecommendationStrategyFactory strategyFactory
        +createTask(TaskRequestDto) TaskResponseDto
        +listTasks(TaskStatus, Pageable) Page~TaskResponseDto~
        +updateTaskStatus(String, TaskStatus) TaskResponseDto
        +deleteTask(String) void
        +recommendNextPendingTask(String) TaskResponseDto
    }

    class TaskRecommendationStrategyFactory {
        -Map~String, TaskRecommendationStrategy~ strategies
        +getStrategy(String) TaskRecommendationStrategy
    }

    class TaskRecommendationStrategy {
        <<interface>>
        +recommendNextTask(List~Task~) Optional~Task~
        +getStrategyType() String
    }

    class DefaultRecommendationStrategy {
        +recommendNextTask(List~Task~) Optional~Task~
        +getStrategyType() String
        -getPriorityWeight(TaskPriority) int
    }

    class SjfRecommendationStrategy {
        +recommendNextTask(List~Task~) Optional~Task~
        +getStrategyType() String
    }

    TaskController --> TaskService : uses
    TaskService --> TaskRecommendationStrategyFactory : uses
    TaskRecommendationStrategyFactory --> TaskRecommendationStrategy : creates/resolves
    DefaultRecommendationStrategy ..|> TaskRecommendationStrategy : implements
    SjfRecommendationStrategy ..|> TaskRecommendationStrategy : implements
    TaskService --> TaskRepository : uses
```

---

## Architectural Trade-offs

During implementation, we weighed several core architectural trade-offs:

1. **Database Selection (H2 File-based vs. PostgreSQL/MySQL Docker)**:
   - *Trade-off*: We chose an **H2 file-backed local database** (`tasksdb`).
   - *Pros*: Zero setup overhead for the reviewer; starts instantly with the application and persists data across restarts.
   - *Cons*: Not suitable for horizontally scaled production clouds (which require central databases like PostgreSQL).
   
2. **Recommendation Sorting Location (In-Memory Java Stream Comparator vs. SQL/JPQL ORDER BY)**:
   - *Trade-off*: We fetch pending tasks into memory and sort them via a Java `Comparator` in the Strategy bean.
   - *Pros*: Keeps Business Logic decoupled from the Database Layer (fulfilling DDD/SOLID). Allows us to easily swap or test strategies in unit tests without database mocks.
   - *Cons*: If there are millions of *pending* tasks, pulling them all into memory to sort in Java will cause latency. If pending tasks grew past $\approx 10,000$, we would push the sorting to the database level via a custom JPQL query.

3. **Topological Sort Implementation (DFS vs. Kahn's Algorithm)**:
   - *Trade-off*: We selected a DFS-based traversal with `vis` and `visiting` arrays.
   - *Pros*: Detects cyclic loops immediately (as soon as a back-edge is hit) and returns the exact node causing the loop, instead of waiting for the full queue to run.
   - *Cons*: Deeply nested linear graphs (e.g. 10,000 tasks in a single line) could cause a `StackOverflowError` in recursive DFS. Kahn's algorithm (BFS-queue based) is stack-safe but cycle diagnostics are less immediate.

4. **Extensibility Structure (Strategy Pattern vs. Direct Service Logic)**:
   - *Trade-off*: Created `TaskRecommendationStrategy` instead of coding the sorting logic directly inside `TaskService`.
   - *Pros*: Enforces the Open/Closed Principle. Service remains clean.
   - *Cons*: Adds minor boilerplate (more files and interfaces to manage) for a simple application.

---

## Complexity Analysis (Time & Space)

### Part A - Topological Sort
We implemented topological sorting using a depth-first search (DFS) with a visited array (`vis`) and a loop-checking recursion stack array (`visiting`):
- **Time Complexity**: $\mathcal{O}(V + E)$, where $V$ is the number of tasks and $E$ is the number of dependencies. We visit each task once, and scan its dependency edges once.
- **Space Complexity**: $\mathcal{O}(V + E)$ to store the adjacency list representation, the recursion stack, and the index translation maps.

### Part B - Smart Task Queue API
The recommendation engine retrieves pending tasks and sorts them:
- **Time Complexity**:
  - **CRUD Operations (Create, Update, Delete)**: $\mathcal{O}(1)$ average lookup and writes using database indexes on the primary key.
  - **Next Task Recommendation**: $\mathcal{O}(N \log N)$ where $N$ is the number of pending tasks (due to sorting the tasks using the deterministic comparator).
- **Space Complexity**:
  - **Memory Space**: $\mathcal{O}(N)$ where $N$ is the number of pending tasks loaded into memory for processing.
  - **Database Storage**: $\mathcal{O}(T)$ where $T$ is the total tasks stored in the H2 file database.

---

## API Documentation (Current Implementation)

Below is the specification of the REST API endpoints. You can also view and test these interactively via the built-in Swagger UI.

### 1. Create Task
- **Route**: `POST /api/tasks`
- **Request Body**:
  ```json
  {
    "id": "task-1",
    "title": "Build API",
    "priority": "HIGH",
    "status": "PENDING",
    "dueDate": "2026-08-15T18:00:00",
    "estimatedHours": 4.5
  }
  ```
- **Responses**:
  - `201 Created`: Returns the created task entity with a generation timestamp.
  - `400 Bad Request`: Validation failure (e.g. blank title) or duplicate task ID.

### 2. List Tasks (with optional Status Filter and Pagination)
- **Route**: `GET /api/tasks?status=PENDING&page=0&size=20`
- **Request Parameters**:
  - `status` (Optional): Filter tasks by state (`PENDING`, `IN_PROGRESS`, `COMPLETED`).
  - `page` (Optional, defaults to `0`): Page index to retrieve.
  - `size` (Optional, defaults to `20`): Page size limit.
- **Responses**:
  - `200 OK`: Returns a paginated response wrapper containing the list of tasks inside the `content` key.

### 3. Update Task Status
- **Route**: `PATCH /api/tasks/{id}?status=IN_PROGRESS`
- **Responses**:
  - `200 OK`: Returns the updated task.
  - `404 Not Found`: Task with the specified ID does not exist.

### 4. Delete Task
- **Route**: `DELETE /api/tasks/{id}`
- **Responses**:
  - `204 No Content`: Task deleted successfully.
  - `404 Not Found`: Task does not exist.

### 5. Get Next Recommended Pending Task
- **Route**: `GET /api/tasks/next-recommended`
- **Responses**:
  - `200 OK`: Returns the highest-priority pending task based on deterministic sorting rules.
  - `404 Not Found`: No pending tasks exist.

---

## Setup and Running Instructions

### Prerequisites
- JDK 17 must be installed and configured on your system.

### 1. Compile and Run Tests
To run all 27 automated unit and integration tests:
```bash
./mvnw clean test
```

### 2. Run the Spring Boot Server
To start the REST API on port `8080`:
```bash
./mvnw spring-boot:run
```

### 3. Verify Endpoints & Interactive API Docs
- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:file:./data/tasksdb`, Username: `sa`, Password: `password`).
