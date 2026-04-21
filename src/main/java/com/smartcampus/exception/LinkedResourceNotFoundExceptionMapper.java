package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 422);
        error.put("error", "Unprocessable Entity");
        error.put("message", "The roomId '" + e.getRoomId() + "' referenced in the " +
                "request body does not exist. Please provide a valid room ID.");
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(error).build();
    }
}
