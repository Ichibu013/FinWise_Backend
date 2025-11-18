package com.fintech.backend.config.Exceptions;

public class ProfileUploadFailedException extends RuntimeException {
    public ProfileUploadFailedException(String message) {
        super(message);
    }
}
