package com.overt.assessment.taskqueue.service;

import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskPriority;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultRecommendationStrategyTest {

    private DefaultRecommendationStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultRecommendationStrategy();
    }

    @Test
    void shouldReturnEmptyWhenNoTasksProvided() {
        assertThat(strategy.recommendNextTask(List.of())).isEmpty();
        assertThat(strategy.recommendNextTask(null)).isEmpty();
    }

    @Test
    void shouldPrioritizeCriticalTasksOverHigh() {
        // Arrange
        Task highTask = Task.builder()
            .id("1")
            .title("High Priority")
            .priority(TaskPriority.HIGH)
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        Task criticalTask = Task.builder()
            .id("2")
            .title("Critical Priority")
            .priority(TaskPriority.CRITICAL)
            .status(TaskStatus.PENDING)
            .createdAt(LocalDateTime.now().plusMinutes(5)) // created later
            .build();

        // Act
        Optional<Task> recommended = strategy.recommendNextTask(List.of(highTask, criticalTask));

        // Assert
        assertThat(recommended).isPresent();
        assertThat(recommended.get().getId()).isEqualTo("2"); // Critical is recommended
    }

    @Test
    void shouldPrioritizeEarlierDueDateOnSamePriority() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task taskFarDue = Task.builder()
            .id("1")
            .title("Far Due")
            .priority(TaskPriority.HIGH)
            .status(TaskStatus.PENDING)
            .dueDate(now.plusDays(10))
            .createdAt(now)
            .build();

        Task taskNearDue = Task.builder()
            .id("2")
            .title("Near Due")
            .priority(TaskPriority.HIGH)
            .status(TaskStatus.PENDING)
            .dueDate(now.plusDays(2))
            .createdAt(now.plusHours(1)) // created later
            .build();

        // Act
        Optional<Task> recommended = strategy.recommendNextTask(List.of(taskFarDue, taskNearDue));

        // Assert
        assertThat(recommended).isPresent();
        assertThat(recommended.get().getId()).isEqualTo("2"); // Near due is recommended
    }

    @Test
    void shouldPrioritizeTasksWithDueDateOverNoDueDate() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        Task noDueTask = Task.builder()
            .id("1")
            .title("No Due")
            .priority(TaskPriority.HIGH)
            .status(TaskStatus.PENDING)
            .createdAt(now)
            .build();

        Task withDueTask = Task.builder()
            .id("2")
            .title("With Due")
            .priority(TaskPriority.HIGH)
            .status(TaskStatus.PENDING)
            .dueDate(now.plusDays(5))
            .createdAt(now.plusHours(1))
            .build();

        // Act
        Optional<Task> recommended = strategy.recommendNextTask(List.of(noDueTask, withDueTask));

        // Assert
        assertThat(recommended).isPresent();
        assertThat(recommended.get().getId()).isEqualTo("2"); // Task with due date is sorted first
    }

    @Test
    void shouldFallbackToCreationTimeIfPriorityAndDueDateAreSame() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(5);
        Task olderTask = Task.builder()
            .id("1")
            .title("Older Task")
            .priority(TaskPriority.MEDIUM)
            .status(TaskStatus.PENDING)
            .dueDate(dueDate)
            .createdAt(now.minusHours(2)) // Created earlier
            .build();

        Task newerTask = Task.builder()
            .id("2")
            .title("Newer Task")
            .priority(TaskPriority.MEDIUM)
            .status(TaskStatus.PENDING)
            .dueDate(dueDate)
            .createdAt(now)
            .build();

        // Act
        Optional<Task> recommended = strategy.recommendNextTask(List.of(newerTask, olderTask));

        // Assert
        assertThat(recommended).isPresent();
        assertThat(recommended.get().getId()).isEqualTo("1"); // Older task recommended
    }
}
