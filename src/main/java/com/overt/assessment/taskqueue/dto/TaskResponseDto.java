package com.overt.assessment.taskqueue.dto;

import com.overt.assessment.taskqueue.entity.Task;
import com.overt.assessment.taskqueue.entity.TaskPriority;
import com.overt.assessment.taskqueue.entity.TaskStatus;
import java.time.LocalDateTime;

public record TaskResponseDto(
    String id,
    String title,
    TaskPriority priority,
    TaskStatus status,
    LocalDateTime dueDate,
    Double estimatedHours,
    LocalDateTime createdAt
) {
    public static TaskResponseDto fromEntity(Task task) {
        return new TaskResponseDto(
            task.getId(),
            task.getTitle(),
            task.getPriority(),
            task.getStatus(),
            task.getDueDate(),
            task.getEstimatedHours(),
            task.getCreatedAt()
        );
    }
}
