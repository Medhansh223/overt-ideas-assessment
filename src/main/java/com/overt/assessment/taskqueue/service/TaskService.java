package com.overt.assessment.taskqueue.service;

import com.overt.assessment.taskqueue.constant.TaskQueueConstants;
import com.overt.assessment.taskqueue.dto.TaskRequestDto;
import com.overt.assessment.taskqueue.dto.TaskResponseDto;
import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import com.overt.assessment.taskqueue.exception.DuplicateTaskIdException;
import com.overt.assessment.taskqueue.exception.TaskNotFoundException;
import com.overt.assessment.taskqueue.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskRecommendationStrategy recommendationStrategy;

    public TaskService(TaskRepository taskRepository, TaskRecommendationStrategy recommendationStrategy) {
        this.taskRepository = taskRepository;
        this.recommendationStrategy = recommendationStrategy;
    }

    @Transactional
    public TaskResponseDto createTask(TaskRequestDto request) {
        logger.info("Creating a new task with title: {}", request.title());
        
        String id = request.id();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        } else if (taskRepository.existsById(id)) {
            String errorMsg = TaskQueueConstants.TASK_ID_CONFLICT + id;
            logger.error("Failed to create task: {}", errorMsg);
            throw new DuplicateTaskIdException(errorMsg);
        }

        Task task = Task.builder()
            .id(id)
            .title(request.title())
            .priority(request.priority())
            .status(request.status())
            .dueDate(request.dueDate())
            .estimatedHours(request.estimatedHours())
            .createdAt(LocalDateTime.now())
            .build();

        Task savedTask = taskRepository.save(task);
        logger.info("Successfully created task with ID: {}", savedTask.getId());
        return TaskResponseDto.fromEntity(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponseDto> listTasks(TaskStatus status, Pageable pageable) {
        logger.info("Fetching paginated tasks with status filter: {} and page settings: {}", status, pageable);
        Page<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }
        return tasks.map(TaskResponseDto::fromEntity);
    }

    @Transactional
    public TaskResponseDto updateTaskStatus(String id, TaskStatus status) {
        logger.info("Updating status of task ID: {} to {}", id, status);
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> {
                String errorMsg = TaskQueueConstants.TASK_NOT_FOUND + id;
                logger.warn("Task update failed: {}", errorMsg);
                return new TaskNotFoundException(errorMsg);
            });

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        logger.info("Successfully updated status of task ID: {} to {}", id, status);
        return TaskResponseDto.fromEntity(updatedTask);
    }

    @Transactional
    public void deleteTask(String id) {
        logger.info("Request received to delete task ID: {}", id);
        if (!taskRepository.existsById(id)) {
            String errorMsg = TaskQueueConstants.TASK_NOT_FOUND + id;
            logger.warn("Task deletion failed: {}", errorMsg);
            throw new TaskNotFoundException(errorMsg);
        }
        taskRepository.deleteById(id);
        logger.info("Successfully deleted task ID: {}", id);
    }

    @Transactional(readOnly = true)
    public TaskResponseDto recommendNextPendingTask() {
        logger.info("Executing recommendation engine to find next pending task");
        List<Task> pendingTasks = taskRepository.findByStatus(TaskStatus.PENDING);
        
        Task recommendedTask = recommendationStrategy.recommendNextTask(pendingTasks)
            .orElseThrow(() -> {
                String errorMsg = "No pending tasks found to recommend.";
                logger.info(errorMsg);
                return new TaskNotFoundException(errorMsg);
            });

        logger.info("Recommendation engine recommended task: {} with ID: {}", recommendedTask.getTitle(), recommendedTask.getId());
        return TaskResponseDto.fromEntity(recommendedTask);
    }
}
