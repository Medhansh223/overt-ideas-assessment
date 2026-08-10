package com.overt.assessment.taskqueue.constant;

public final class TaskQueueConstants {

    private TaskQueueConstants() {
        // Prevent instantiation
    }

    // API Routes
    public static final String API_BASE_PATH = "/api/tasks";
    public static final String RECOMMENDED_PATH = "/next-recommended";
    public static final String ID_PATH_VARIABLE = "/{id}";

    // DB Configurations & Schema names
    public static final String TASK_TABLE_NAME = "tasks";

    // Logging & Validation Error Messages
    public static final String TASK_NOT_FOUND = "Task not found with ID: ";
    public static final String TASK_ID_CONFLICT = "Task ID already exists: ";
    public static final String FIELD_REQUIRED_TITLE = "Task title is required.";
    public static final String FIELD_REQUIRED_PRIORITY = "Task priority is required.";
    public static final String FIELD_REQUIRED_STATUS = "Task status is required.";
    public static final String FIELD_REQUIRED_ESTIMATED_HOURS = "Estimated hours must be greater than or equal to 0.";

    // Sort order values for deterministic matching
    public static final int PRIORITY_WEIGHT_CRITICAL = 4;
    public static final int PRIORITY_WEIGHT_HIGH = 3;
    public static final int PRIORITY_WEIGHT_MEDIUM = 2;
    public static final int PRIORITY_WEIGHT_LOW = 1;
}
