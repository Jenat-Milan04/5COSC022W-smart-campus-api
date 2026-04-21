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
 * RoomNotEmptyExceptionMapper is responsible for converting
 * RoomNotEmptyException into a proper HTTP response.
 * 
 * It acts as a global handler for this specific exception.
 */
@Provider // Registers this class as a JAX-RS provider (auto-detected)
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    /**
     * Converts RoomNotEmptyException into a standardized HTTP response.
     * 
     * @param e the thrown RoomNotEmptyException
     * @return Response object with structured JSON error message
     */
    @Override
    public Response toResponse(RoomNotEmptyException e) {

        // Create a map to structure the error response
        Map<String, Object> error = new HashMap<>();

        // Add HTTP status code
        error.put("status", 409);

        // Add short error type
        error.put("error", "Conflict");

        // Add detailed, user-friendly error message
        error.put("message",
                "Room '" + e.getRoomId() + "' cannot be deleted. " +
                        "It still has active sensors assigned. Please remove all sensors first.");

        // Return HTTP 409 Conflict with JSON error body
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}