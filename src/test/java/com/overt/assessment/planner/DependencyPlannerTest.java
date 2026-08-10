package com.overt.assessment.planner;

import com.overt.assessment.planner.exception.CircularDependencyException;
import com.overt.assessment.planner.exception.DependencyPlannerException;
import com.overt.assessment.planner.exception.MissingDependencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DependencyPlannerTest {

    private DependencyPlanner planner;

    @BeforeEach
    void setUp() {
        planner = new DependencyPlanner();
    }

    @Test
    void shouldReturnValidExecutionOrderForValidGraph() {
        // Arrange
        TaskNode design = new TaskNode("design", List.of());
        TaskNode api = new TaskNode("api", List.of("design"));
        TaskNode ui = new TaskNode("ui", List.of("design"));
        TaskNode release = new TaskNode("release", List.of("api", "ui"));

        List<TaskNode> tasks = List.of(design, api, ui, release);

        // Act
        List<String> order = planner.planExecutionOrder(tasks);

        // Assert
        assertThat(order).hasSize(4);
        assertThat(order.indexOf("design")).isLessThan(order.indexOf("api"));
        assertThat(order.indexOf("design")).isLessThan(order.indexOf("ui"));
        assertThat(order.indexOf("api")).isLessThan(order.indexOf("release"));
        assertThat(order.indexOf("ui")).isLessThan(order.indexOf("release"));
    }

    @Test
    void shouldReturnValidOrderForIndependentTasks() {
        // Arrange
        TaskNode task1 = new TaskNode("task1", List.of());
        TaskNode task2 = new TaskNode("task2", List.of());
        TaskNode task3 = new TaskNode("task3", List.of());

        List<TaskNode> tasks = List.of(task1, task2, task3);

        // Act
        List<String> order = planner.planExecutionOrder(tasks);

        // Assert
        assertThat(order).containsExactlyInAnyOrder("task1", "task2", "task3");
    }

    @Test
    void shouldThrowCircularDependencyExceptionOnDirectCycle() {
        // Arrange
        TaskNode taskA = new TaskNode("A", List.of("B"));
        TaskNode taskB = new TaskNode("B", List.of("A"));

        List<TaskNode> tasks = List.of(taskA, taskB);

        // Act & Assert
        assertThatThrownBy(() -> planner.planExecutionOrder(tasks))
                .isInstanceOf(CircularDependencyException.class)
                .hasMessageContaining("Circular dependency detected");
    }

    @Test
    void shouldThrowCircularDependencyExceptionOnTransitiveCycle() {
        // Arrange
        TaskNode taskA = new TaskNode("A", List.of("B"));
        TaskNode taskB = new TaskNode("B", List.of("C"));
        TaskNode taskC = new TaskNode("C", List.of("A"));

        List<TaskNode> tasks = List.of(taskA, taskB, taskC);

        // Act & Assert
        assertThatThrownBy(() -> planner.planExecutionOrder(tasks))
                .isInstanceOf(CircularDependencyException.class)
                .hasMessageContaining("Circular dependency detected");
    }

    @Test
    void shouldThrowMissingDependencyExceptionWhenNodeReferencedDoesNotExist() {
        // Arrange
        TaskNode taskA = new TaskNode("A", List.of("B")); // B does not exist in the list

        List<TaskNode> tasks = List.of(taskA);

        // Act & Assert
        assertThatThrownBy(() -> planner.planExecutionOrder(tasks))
                .isInstanceOf(MissingDependencyException.class)
                .hasMessageContaining("Missing dependency task reference");
    }

    @Test
    void shouldThrowExceptionWhenTaskListIsNull() {
        assertThatThrownBy(() -> planner.planExecutionOrder(null))
                .isInstanceOf(DependencyPlannerException.class)
                .hasMessageContaining("cannot be null");
    }

    @Test
    void shouldThrowExceptionWhenDuplicateTaskIdsExist() {
        // Arrange
        TaskNode task1 = new TaskNode("A", List.of());
        TaskNode task2 = new TaskNode("A", List.of());

        List<TaskNode> tasks = List.of(task1, task2);

        // Act & Assert
        assertThatThrownBy(() -> planner.planExecutionOrder(tasks))
                .isInstanceOf(DependencyPlannerException.class)
                .hasMessageContaining("Task IDs must be unique");
    }
}
