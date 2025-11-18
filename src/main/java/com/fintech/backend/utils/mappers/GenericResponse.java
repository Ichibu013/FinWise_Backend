package com.fintech.backend.utils.mappers;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * GenericResponse class to handle GenericResponse
 * @param <T> the type parameter
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API Response Wrapper")
public class GenericResponse<T> implements Serializable {

    @Schema(description = "Request ID for tracking")
    private String requestId;

    @Schema(description = "Response message")
    private String message;

    @Schema(description = "Success flag")
    private Boolean success;

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Error message")
    private String errorMessage;

    @Schema(description = "Error details")
    private Map<String, String> errorDetails;  // Renamed field

    @Schema(description = "Response timestamp")
    private String timestamp;




    /**
     * Constructor for success response.
     * @param data the data
     * @param successMessage the success message
     * @param messageSource the message source
     * @param args the message params
     */
    public GenericResponse(final T data, final String successMessage, MessageSource messageSource, Object... args) {
        this.success = true;
        this.message = messageSource.getMessage(successMessage, args, LocaleContextHolder.getLocale());
        this.data = data;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC).toString();
    }

    /**
     * Constructor for error response.
     * @param errorDetails the error details
     * @param errorMessage the error message
     * @param messageSource the message source
     * @param args the message params
     */
    public GenericResponse(final Map<String, String> errorDetails, String errorMessage, MessageSource messageSource,
                           Object... args) {
        this.errorDetails = errorDetails;
        this.message = messageSource.getMessage(errorMessage, args, LocaleContextHolder.getLocale());
        this.success = false;
        this.timestamp = LocalDateTime.now(ZoneOffset.UTC).toString();
    }
}

