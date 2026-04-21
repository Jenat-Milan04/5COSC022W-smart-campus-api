package com.smartcampus.resource;

// Import JAX-RS annotations and classes for RESTful web services
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Import utility classes for creating key-value data structures
import java.util.HashMap;
import java.util.Map;

/**
 * DiscoveryResource class acts as the root endpoint of the Smart Campus API.
 * It provides general information about the API and available resources.
 */
@Path("/")  // Defines the base URI path for this resource
public class DiscoveryResource {

    /**
     * Handles HTTP GET requests to the root endpoint.
     * 
     * @return Response object containing API metadata and available resource links in JSON format
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON) // Specifies that the response will be in JSON format
    public Response discover() {

        // Create a map to hold the overall response data
        Map<String, Object> response = new HashMap<>();

        // Add basic API information
        response.put("api", "Smart Campus API"); // Name of the API
        response.put("version", "1.0"); // Current version of the API
        response.put("contact", "admin@smartcampus.ac.uk"); // Contact email for support

        // Create a map to store available resource endpoints
        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms"); // Endpoint for room-related data
        resources.put("sensors", "/api/v1/sensors"); // Endpoint for sensor-related data

        // Add resources map to the main response
        response.put("resources", resources);

        // Create a map for hypermedia links (HATEOAS style navigation)
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1"); // Link to the current API root
        links.put("rooms", "/api/v1/rooms"); // Link to rooms resource
        links.put("sensors", "/api/v1/sensors"); // Link to sensors resource

        // Add links map to the main response under "_links"
        response.put("_links", links);

        // Build and return HTTP response with status 200 (OK) and JSON body
        return Response.ok(response).build();
    }
}