package com.smartcampus.filter;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

// Marks this class as a JAX-RS provider (auto-detected filter)
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Logger used to print request/response logs
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    // =========================
    // REQUEST INTERCEPTOR
    // =========================
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Log incoming HTTP request method + URL
        LOGGER.info("[REQUEST]  " +
                requestContext.getMethod() + " " +
                requestContext.getUriInfo().getRequestUri());
    }

    // =========================
    // RESPONSE INTERCEPTOR
    // =========================
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {

        // Log outgoing response status + request details
        LOGGER.info("[RESPONSE] Status: " +
                responseContext.getStatus() + " for " +
                requestContext.getMethod() + " " +
                requestContext.getUriInfo().getRequestUri());
    }
}