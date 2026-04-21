package com.smartcampus.resource;

// Import custom exception for handling missing linked resources (e.g., room not found)
import com.smartcampus.exception.LinkedResourceNotFoundException;

// Import Sensor model representing sensor entity
import com.smartcampus.model.Sensor;

// Import singleton data store for managing application data
import com.smartcampus.store.DataStore;

// Import JAX-RS annotations and classes for RESTful services
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Import for creating URI of newly created resources
import java.net.URI;

// Import list and stream utilities for filtering data
import java.util.List;
import java.util.stream.Collectors;

/**
 * SensorResource class provides RESTful endpoints for managing sensors.
 * It supports retrieving, creating, and accessing sensor-related sub-resources.
 */
@Path("/sensors") // Base URI path for sensor-related operations
@Produces(MediaType.APPLICATION_JSON) // All responses returned as JSON
@Consumes(MediaType.APPLICATION_JSON) // Accepts JSON input
public class SensorResource {

    // Singleton instance of DataStore
    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     * Retrieves all sensors or filters them by type.
     * 
     * @param type Optional query parameter to filter sensors by type
     * @return Response containing list of sensors
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {

        // Stream through all sensors and filter by type if provided
        List<Sensor> sensors = store.getSensors().values().stream()
                .filter(s -> type == null || s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());

        // Return HTTP 200 OK with filtered sensor list
        return Response.ok(sensors).build();
    }

    /**
     * POST /api/v1/sensors
     * Creates a new sensor and links it to a room.
     * 
     * @param sensor Sensor object received from client
     * @return Response with created sensor or error message
     */
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate Sensor ID
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST) // 400 Bad Request
                    .entity("{\"error\":\"Sensor ID is required\"}")
                    .build();
        }

        // Validate if linked room exists
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            // Throw custom exception if room does not exist
            throw new LinkedResourceNotFoundException(sensor.getRoomId());
        }

        // Check for duplicate sensor ID
        if (store.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT) // 409 Conflict
                    .entity("{\"error\":\"Sensor with this ID already exists\"}")
                    .build();
        }

        // Set default status if not provided
        if (sensor.getStatus() == null) {
            sensor.setStatus("ACTIVE");
        }

        // Add sensor to data store
        store.getSensors().put(sensor.getId(), sensor);

        // Link sensor to its room (add sensor ID to room's sensor list)
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        // Return HTTP 201 Created with location header
        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
                .build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Retrieves a specific sensor by ID.
     * 
     * @param sensorId ID of the sensor
     * @return Response with sensor data or error if not found
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        // Retrieve sensor from data store
        Sensor sensor = store.getSensors().get(sensorId);

        // If sensor not found, return 404 Not Found
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Return HTTP 200 OK with sensor data
        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for sensor readings
     * GET /api/v1/sensors/{sensorId}/readings
     * 
     * @param sensorId ID of the sensor
     * @return SensorReadingResource instance for handling readings
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {

        // Returns a new resource object to handle sensor readings
        return new SensorReadingResource(sensorId);
    }
}