package com.overt.assessment.taskqueue.controller;

import com.overt.assessment.taskqueue.constant.TaskQueueConstants;
import com.overt.assessment.taskqueue.dto.TaskRequestDto;
import com.overt.assessment.taskqueue.dto.TaskResponseDto;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import com.overt.assessment.taskqueue.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(TaskQueueConstants.API_BASE_PATH)
@Tag(name = "Smart Task Queue API", description = "Endpoints for managing and prioritising development tasks.")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task. Generates a random UUID if ID is not provided.")
    @ApiResponse(responseCode = "201", description = "Task created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload or duplicate task ID")
    public ResponseEntity<TaskResponseDto> createTask(@Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto createdTask = taskService.createTask(request);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List tasks", description = "Lists all tasks with optional status filter.")
    @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    public ResponseEntity<List<TaskResponseDto>> listTasks(@RequestParam(required = false) TaskStatus status) {
        List<TaskResponseDto> tasks = taskService.listTasks(status);
        return ResponseEntity.ok(tasks);
    }

    @PatchMapping(TaskQueueConstants.ID_PATH_VARIABLE)
    @Operation(summary = "Update task status", description = "Updates status of a task by ID.")
    @ApiResponse(responseCode = "200", description = "Task status updated successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable String id,
            @RequestParam TaskStatus status) {
        TaskResponseDto updatedTask = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping(TaskQueueConstants.ID_PATH_VARIABLE)
    @Operation(summary = "Delete task", description = "Deletes a task by ID.")
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @ApiResponse(responseCode = "404", description = "Task not found")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(TaskQueueConstants.RECOMMENDED_PATH)
    @Operation(summary = "Get next recommended pending task", description = "Returns the highest priority pending task based on deterministic sorting rules.")
    @ApiResponse(responseCode = "200", description = "Recommended task retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No pending tasks found")
    public ResponseEntity<TaskResponseDto> getNextRecommendedPendingTask() {
        TaskResponseDto recommendedTask = taskService.recommendNextPendingTask();
        return ResponseEntity.ok(recommendedTask);
    }
}
