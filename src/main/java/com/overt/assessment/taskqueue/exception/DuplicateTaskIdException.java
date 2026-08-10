package com.overt.assessment.taskqueue.exception;

public class DuplicateTaskIdException extends RuntimeException {
    public DuplicateTaskIdException(String message) {
        super(message);
    }
}
