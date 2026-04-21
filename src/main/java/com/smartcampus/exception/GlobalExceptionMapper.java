package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * GlobalExceptionMapper is a fallback exception handler for all unhandled exceptions.
 *
 * It ensures that unexpected server errors are caught and returned as clean,
 * structured JSON responses. JAX-RS WebApplicationExceptions (e.g. 404, 405)
 * are passed through with their original status codes.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {

        // If Jersey/JAX-RS already assigned an HTTP status (e.g. 404 Not Found,
        // 405 Method Not Allowed), respect it — don't override with 500.
        if (e instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) e;
            Response original = wae.getResponse();
            int status = original.getStatus();

            // Only log server-side errors (5xx); client errors (4xx) are expected
            if (status >= 500) {
                LOGGER.severe("Server error: " + e.getMessage());
            }

            // Build a consistent JSON body while keeping the original status code
            Map<String, Object> error = new HashMap<>();
            error.put("status", status);
            error.put("error", Response.Status.fromStatusCode(status) != null
                    ? Response.Status.fromStatusCode(status).getReasonPhrase()
                    : "Error");
            error.put("message", e.getMessage() != null
                    ? e.getMessage()
                    : "No additional details available.");

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(error)
                    .build();
        }

        // Truly unexpected exception — log it and return 500
        LOGGER.severe("Unexpected error: " + e.getMessage());

        Map<String, Object> error = new HashMap<>();
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message",
                "An unexpected error occurred. Please contact the system administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(error)
                .build();
    }
}