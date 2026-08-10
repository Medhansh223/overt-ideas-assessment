package com.overt.assessment.planner;

import java.util.List;

/**
 * Represents a node in the dependency graph.
 * 
 * @param id The unique identifier of the task.
 * @param dependsOn The list of task IDs that must be completed before this task.
 */
public record TaskNode(String id, List<String> dependsOn) {

    public TaskNode {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Task ID cannot be null or blank");
        }
        if (dependsOn == null) {
            dependsOn = List.of();
        }
    }
}
