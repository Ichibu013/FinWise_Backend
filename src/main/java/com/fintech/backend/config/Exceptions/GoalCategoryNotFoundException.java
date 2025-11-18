package com.fintech.backend.config.Exceptions;

public class GoalCategoryNotFoundException extends RuntimeException {
    public GoalCategoryNotFoundException(String message) {
        super(message);
    }
}
