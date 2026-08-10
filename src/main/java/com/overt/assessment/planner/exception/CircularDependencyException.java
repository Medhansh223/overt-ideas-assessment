package com.overt.assessment.planner.exception;

public class CircularDependencyException extends DependencyPlannerException {
    public CircularDependencyException(String message) {
        super(message);
    }
}
