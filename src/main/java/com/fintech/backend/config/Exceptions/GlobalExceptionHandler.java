package com.fintech.backend.config.Exceptions;

import com.fintech.backend.controllers.FormattedResponseMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {
    private final FormattedResponseMapping formattedResponseMapping;

    public GlobalExceptionHandler(FormattedResponseMapping formattedResponseMapping) {
        this.formattedResponseMapping = formattedResponseMapping;
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<HashMap<String, Object>> handleTransactionNotFoundException(TransactionNotFoundException e) {
        return formattedResponseMapping.getResponseFormat(HttpStatus.NOT_FOUND,
                "Transaction Not Found",
                e.getMessage());
    }

    @ExceptionHandler(GoalCategoryNotFoundException.class)
    public ResponseEntity<HashMap<String,Object>> handleGoalCategoryNotFoundException(GoalCategoryNotFoundException e){
        return formattedResponseMapping.getResponseFormat(HttpStatus.NOT_FOUND,
                "Goal Category Not Found",
                e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HashMap<String, Object>> handleUserNotFoundException(UserNotFoundException e) {
        return formattedResponseMapping.getResponseFormat(HttpStatus.NOT_FOUND,
                "User Not Found",
                e.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<HashMap<String, Object>> handleInvalidPasswordException(InvalidPasswordException e) {
        return formattedResponseMapping.getResponseFormat(
                HttpStatus.UNAUTHORIZED,
                "Invalid Password",
                e.getMessage());
    }

    @ExceptionHandler(UserExistsException.class)
    public ResponseEntity<HashMap<String, Object>> handleUserExistsException(UserExistsException e) {
        return formattedResponseMapping.getResponseFormat(HttpStatus.CONFLICT,
                "User Already Exists",
                e.getMessage());
    }

    @ExceptionHandler(ProfileUploadFailedException.class)
    public ResponseEntity<HashMap<String, Object>> handleProfileUploadFailedException(ProfileUploadFailedException e) {
        return formattedResponseMapping.getResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR,
                "Profile Upload Failed",
                e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HashMap<String, Object>> handleAllExceptions(Exception e) {
        return formattedResponseMapping.getResponseFormat(HttpStatus.INTERNAL_SERVER_ERROR,
                e.getClass().getSimpleName(),
                e.getMessage());
    }
}
