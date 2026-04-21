package com.smartcampus.exception;

// JAX-RS imports for building HTTP responses and registering providers
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Utility imports for structured error handling and logging
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * GlobalExceptionMapper is a fallback exception handler for all unhandled exceptions.
 * 
 * It ensures that any unexpected server error is caught and returned
 * as a clean, structured JSON response instead of exposing stack traces.
 */
@Provider // Registers this as a global JAX-RS exception handler
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    // Logger used to record unexpected system errors for debugging
    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    /**
     * Handles all unhandled exceptions in the application.
     * 
     * @param e the thrown exception (any Throwable)
     * @return standardized HTTP 500 response
     */
    @Override
    public Response toResponse(Throwable e) {

        // Log the error for server-side debugging
        LOGGER.severe("Unexpected error: " + e.getMessage());

        // Create a structured error response body
        Map<String, Object> error = new HashMap<>();

        // HTTP status code
        error.put("status", 500);

        // Short error type
        error.put("error", "Internal Server Error");

        // Generic user-friendly message (avoid exposing system details)
        error.put("message",
                "An unexpected error occurred. Please contact the system administrator.");

        // Return HTTP 500 response with JSON error body
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}