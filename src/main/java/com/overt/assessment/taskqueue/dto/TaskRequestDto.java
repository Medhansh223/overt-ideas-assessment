package com.overt.assessment.taskqueue.dto;

import com.overt.assessment.taskqueue.constant.TaskQueueConstants;
import com.overt.assessment.taskqueue.entity.TaskPriority;
import com.overt.assessment.taskqueue.entity.TaskStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TaskRequestDto(
    String id,

    @NotBlank(message = TaskQueueConstants.FIELD_REQUIRED_TITLE)
    String title,

    @NotNull(message = TaskQueueConstants.FIELD_REQUIRED_PRIORITY)
    TaskPriority priority,

    @NotNull(message = TaskQueueConstants.FIELD_REQUIRED_STATUS)
    TaskStatus status,

    LocalDateTime dueDate,

    @DecimalMin(value = "0.0", inclusive = false, message = "Estimated hours must be greater than 0")
    @NotNull(message = "Estimated hours is required")
    Double estimatedHours
) {}
