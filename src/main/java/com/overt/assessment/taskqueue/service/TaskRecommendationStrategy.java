package com.overt.assessment.taskqueue.service;

import com.overt.assessment.taskqueue.entity.Task;
import java.util.List;
import java.util.Optional;

/**
 * Strategy interface to allow hot swapping recommendation algorithms.
 */
public interface TaskRecommendationStrategy {
    Optional<Task> recommendNextTask(List<Task> pendingTasks);
}
