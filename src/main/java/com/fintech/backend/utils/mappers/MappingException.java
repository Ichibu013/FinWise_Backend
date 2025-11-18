package com.fintech.backend.utils.mappers;

/**
 * Custom exception class for handling mapping-related errors.
 * This is an unchecked exception, meaning it extends RuntimeException,
 * and typically indicates a programming error or misconfiguration
 * rather than a recoverable condition.
 */
public class MappingException extends RuntimeException {

    /**
     * Constructs a new MappingException with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the getMessage() method).
     */
    public MappingException(String message) {
        super(message);
    }

    /**
     * Constructs a new MappingException with the specified detail message and
     * cause.
     *
     * @param message The detail message.
     * @param cause   The cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MappingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new MappingException with the specified cause and a
     * detail message of (cause==null ? null : cause.toString()) (which
     * typically contains the class and detail message of cause).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables.
     *
     * @param cause The cause (which is saved for later retrieval by the getCause() method).
     * (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public MappingException(Throwable cause) {
        super(cause);
    }
}
