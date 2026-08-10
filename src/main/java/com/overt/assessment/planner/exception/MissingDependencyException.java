package com.overt.assessment.planner.exception;

public class MissingDependencyException extends DependencyPlannerException {
    public MissingDependencyException(String message) {
        super(message);
    }
}
