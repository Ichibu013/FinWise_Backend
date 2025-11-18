package com.fintech.backend.utils.mappers;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GenericResponseFactory class to handle GenericResponse wrapper
 */
@Getter
@Component
public class GenericResponseFactory {

    private final MessageSource messageSource;

    @Autowired
    public GenericResponseFactory(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * successResponse
     *
     * @param <T> the type parameter
     * @return the generic response
     */
    public <T> GenericResponse<T> successResponse(final T data, final String messageKey) {
        return new GenericResponse<>(data, messageKey, messageSource);
    }

    /**
     * successResponse
     *
     * @param <T> the type parameter
     * @return the generic response
     */
    public <T> GenericResponse<T> successResponse(final T data, final String messageKey, final Object... args) {
        return new GenericResponse<>(data, messageKey, messageSource, args);
    }

    /**
     * errorResponse
     *
     * @param <T> the type parameter
     * @return the generic response
     */
    public <T> GenericResponse<T> errorResponse(final Map<String, String> errors, final String messageKey) {
        return new GenericResponse<>(errors, messageKey, messageSource);
    }

    /**
     * errorResponse
     *
     * @param <T> the type parameter
     * @return the generic response
     */
    public <T> GenericResponse<T> errorResponse(final Map<String, String> errors, final String messageKey,
                                                final Object... args) {
        return new GenericResponse<>(errors, messageKey, messageSource, args);
    }

}
