package com.smartcampus.exception;

// Import JAX-RS classes for HTTP response handling
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Import ExceptionMapper and Provider for global exception handling
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Import utility classes for creating structured error responses
import java.util.HashMap;
import java.util.Map;

/**
 * LinkedResourceNotFoundExceptionMapper handles
 * LinkedResourceNotFoundException globally.
 * 
 * It converts the exception into a structured HTTP response
 * with a meaningful error message.
 */
@Provider // Registers this class as a JAX-RS provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    /**
     * Converts LinkedResourceNotFoundException into HTTP response.
     * 
     * @param e the thrown exception
     * @return Response with JSON error details
     */
    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {

        // Create a map to hold structured error details
        Map<String, Object> error = new HashMap<>();

        // Add HTTP status code
        error.put("status", 422);

        // Add short error type
        error.put("error", "Unprocessable Entity");

        // Add detailed explanation for client
        error.put("message",
                "The roomId '" + e.getRoomId() + "' referenced in the " +
                "request body does not exist. Please provide a valid room ID.");

        // Return HTTP 422 with JSON response body
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}