package com.overt.assessment.taskqueue.service;

import com.overt.assessment.taskqueue.dto.TaskRequestDto;
import com.overt.assessment.taskqueue.dto.TaskResponseDto;
import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskPriority;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import com.overt.assessment.taskqueue.exception.DuplicateTaskIdException;
import com.overt.assessment.taskqueue.exception.TaskNotFoundException;
import com.overt.assessment.taskqueue.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskRecommendationStrategy recommendationStrategy;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        recommendationStrategy = mock(TaskRecommendationStrategy.class);
        taskService = new TaskService(taskRepository, recommendationStrategy);
    }

    @Test
    void shouldCreateTaskAndSave() {
        // Arrange
        TaskRequestDto request = new TaskRequestDto("test-task", "Write Code", TaskPriority.MEDIUM, TaskStatus.PENDING, null, 2.5);
        Task expectedTask = Task.builder()
            .id("test-task")
            .title("Write Code")
            .priority(TaskPriority.MEDIUM)
            .status(TaskStatus.PENDING)
            .estimatedHours(2.5)
            .build();

        when(taskRepository.existsById("test-task")).thenReturn(false);
        when(taskRepository.save(any(Task.class))).thenReturn(expectedTask);

        // Act
        TaskResponseDto response = taskService.createTask(request);

        // Assert
        assertThat(response.id()).isEqualTo("test-task");
        assertThat(response.title()).isEqualTo("Write Code");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingTaskWithDuplicateId() {
        // Arrange
        TaskRequestDto request = new TaskRequestDto("test-task", "Write Code", TaskPriority.MEDIUM, TaskStatus.PENDING, null, 2.5);
        when(taskRepository.existsById("test-task")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskService.createTask(request))
            .isInstanceOf(DuplicateTaskIdException.class)
            .hasMessageContaining("Task ID already exists");
    }

    @Test
    void shouldListTasksSuccessfully() {
        // Arrange
        Task task = Task.builder()
            .id("task-id")
            .title("Read code")
            .status(TaskStatus.PENDING)
            .build();
        PageRequest pageRequest = PageRequest.of(0, 20);
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(task), pageRequest, 1));

        // Act
        Page<TaskResponseDto> response = taskService.listTasks(null, pageRequest);

        // Assert
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).id()).isEqualTo("task-id");
        verify(taskRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void shouldUpdateTaskStatusSuccessfully() {
        // Arrange
        Task existingTask = Task.builder()
            .id("task-id")
            .title("Clean codebase")
            .priority(TaskPriority.LOW)
            .status(TaskStatus.PENDING)
            .build();

        when(taskRepository.findById("task-id")).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        TaskResponseDto response = taskService.updateTaskStatus("task-id", TaskStatus.IN_PROGRESS);

        // Assert
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    void shouldThrowExceptionOnUpdateIfTaskNotFound() {
        when(taskRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTaskStatus("invalid", TaskStatus.COMPLETED))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");
    }

    @Test
    void shouldDeleteTaskSuccessfully() {
        // Arrange
        when(taskRepository.existsById("task-id")).thenReturn(true);

        // Act
        taskService.deleteTask("task-id");

        // Assert
        verify(taskRepository, times(1)).deleteById("task-id");
    }

    @Test
    void shouldThrowExceptionOnDeleteIfTaskNotFound() {
        when(taskRepository.existsById("invalid")).thenReturn(false);

        assertThatThrownBy(() -> taskService.deleteTask("invalid"))
            .isInstanceOf(TaskNotFoundException.class)
            .hasMessageContaining("Task not found");
    }

    @Test
    void shouldRecommendNextTaskBasedOnStrategy() {
        // Arrange
        Task pendingTask = Task.builder()
            .id("1")
            .title("Pending Task")
            .status(TaskStatus.PENDING)
            .build();

        when(taskRepository.findByStatus(TaskStatus.PENDING)).thenReturn(List.of(pendingTask));
        when(recommendationStrategy.recommendNextTask(any())).thenReturn(Optional.of(pendingTask));

        // Act
        TaskResponseDto recommended = taskService.recommendNextPendingTask();

        // Assert
        assertThat(recommended.id()).isEqualTo("1");
        verify(recommendationStrategy, times(1)).recommendNextTask(any());
    }
}
