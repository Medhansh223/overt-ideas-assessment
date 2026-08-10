package com.overt.assessment.taskqueue.service;

import com.overt.assessment.taskqueue.constant.TaskQueueConstants;
import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskPriority;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Default deterministic task selection strategy based on:
 * 1. Priority (Critical > High > Medium > Low)
 * 2. Due Date (Earliest first, null due dates pushed to the end)
 * 3. Creation Time (Oldest first)
 */
@Component
public class DefaultRecommendationStrategy implements TaskRecommendationStrategy {

    @Override
    public Optional<Task> recommendNextTask(List<Task> pendingTasks) {
        if (pendingTasks == null || pendingTasks.isEmpty()) {
            return Optional.empty();
        }

        // Custom comparator enforcing deterministic rule
        Comparator<Task> taskComparator = Comparator
            .comparingInt((Task t) -> getPriorityWeight(t.getPriority()))
            .reversed() // Highest priority weight first (Critical=4, High=3...)
            .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())) // Earliest due dates first, nulls at the end
            .thenComparing(Task::getCreatedAt); // Oldest created tasks first (FIFO tie-breaker)

        return pendingTasks.stream().min(taskComparator);
    }

    private int getPriorityWeight(TaskPriority priority) {
        if (priority == null) {
            return TaskQueueConstants.PRIORITY_WEIGHT_LOW;
        }
        return switch (priority) {
            case CRITICAL -> TaskQueueConstants.PRIORITY_WEIGHT_CRITICAL;
            case HIGH -> TaskQueueConstants.PRIORITY_WEIGHT_HIGH;
            case MEDIUM -> TaskQueueConstants.PRIORITY_WEIGHT_MEDIUM;
            case LOW -> TaskQueueConstants.PRIORITY_WEIGHT_LOW;
        };
    }
}
