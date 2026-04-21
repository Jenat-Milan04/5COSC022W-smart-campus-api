package com.smartcampus.exception;

// Import JAX-RS classes for building HTTP responses
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Import ExceptionMapper and Provider for global exception handling
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Import utility classes for structured error response
import java.util.HashMap;
import java.util.Map;

/**
 * SensorUnavailableExceptionMapper handles SensorUnavailableException globally.
 * 
 * It converts the exception into a structured HTTP response,
 * informing the client that the sensor is currently unavailable.
 */
@Provider // Registers this class as a JAX-RS provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    /**
     * Converts SensorUnavailableException into an HTTP response.
     * 
     * @param e the thrown exception
     * @return Response with JSON error details
     */
    @Override
    public Response toResponse(SensorUnavailableException e) {

        // Create a map to structure the error response
        Map<String, Object> error = new HashMap<>();

        // Add HTTP status code
        error.put("status", 403);

        // Add short error description
        error.put("error", "Forbidden");

        // Add detailed message explaining the issue
        error.put("message",
                "Sensor '" + e.getSensorId() + "' is currently under " +
                "MAINTENANCE. It cannot accept new readings.");

        // Return HTTP 403 Forbidden with JSON response body
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}