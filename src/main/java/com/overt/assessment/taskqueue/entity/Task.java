package com.overt.assessment.taskqueue.entity;

import com.overt.assessment.taskqueue.constant.TaskQueueConstants;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = TaskQueueConstants.TASK_TABLE_NAME)
public class Task {

    @Id
    private String id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "estimated_hours")
    private Double estimatedHours;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Task() {}

    public static Builder builder() {
        return new Builder();
    }

    // Builder Pattern Implementation
    public static class Builder {
        private String id;
        private String title;
        private TaskPriority priority;
        private TaskStatus status;
        private LocalDateTime dueDate;
        private Double estimatedHours;
        private LocalDateTime createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder priority(TaskPriority priority) {
            this.priority = priority;
            return this;
        }

        public Builder status(TaskStatus status) {
            this.status = status;
            return this;
        }

        public Builder dueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public Builder estimatedHours(Double estimatedHours) {
            this.estimatedHours = estimatedHours;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Task build() {
            Task task = new Task();
            task.id = this.id;
            task.title = this.title;
            task.priority = this.priority;
            task.status = this.status;
            task.dueDate = this.dueDate;
            task.estimatedHours = this.estimatedHours;
            task.createdAt = this.createdAt != null ? this.createdAt : LocalDateTime.now();
            return task;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Double getEstimatedHours() { return estimatedHours; }
    public void setEstimatedHours(Double estimatedHours) { this.estimatedHours = estimatedHours; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
