package com.overt.assessment.planner.constant;

public final class DependencyPlannerConstants {

    private DependencyPlannerConstants() {
        // Prevent instantiation of utility/constant class
    }

    public static final String CYCLIC_DEPENDENCY_DETECTED = "Circular dependency detected starting from node: ";
    public static final String MISSING_DEPENDENCY_DETECTED = "Missing dependency task reference: ";
    public static final String TASK_LIST_CANNOT_BE_NULL = "Task list for dependency planning cannot be null.";
    public static final String TASK_IDS_MUST_BE_UNIQUE = "Task IDs must be unique. Duplicate ID found: ";
}
