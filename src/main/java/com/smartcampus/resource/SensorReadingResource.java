package com.smartcampus.resource;

// Import custom exception when sensor is not available (e.g., under maintenance)
import com.smartcampus.exception.SensorUnavailableException;

// Import model classes
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

// Import singleton data store
import com.smartcampus.store.DataStore;

// Import JAX-RS annotations and classes
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Import URI for resource creation
import java.net.URI;

// Import list collection
import java.util.List;

/**
 * SensorReadingResource handles operations related to sensor readings.
 * This is a sub-resource class accessed via SensorResource.
 */
@Produces(MediaType.APPLICATION_JSON) // Responses are in JSON format
@Consumes(MediaType.APPLICATION_JSON) // Accepts JSON input
public class SensorReadingResource {

    // ID of the parent sensor (passed from parent resource)
    private final String sensorId;

    // Singleton instance of DataStore
    private final DataStore store = DataStore.getInstance();

    /**
     * Constructor to initialize sensorId when accessed as a sub-resource
     * 
     * @param sensorId ID of the parent sensor
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Retrieves all readings for a specific sensor.
     * 
     * @return Response containing list of sensor readings
     */
    @GET
    public Response getReadings() {

        // Retrieve sensor from data store
        Sensor sensor = store.getSensors().get(sensorId);

        // If sensor not found, return 404 Not Found
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Fetch all readings associated with the sensor
        List<SensorReading> history = store.getReadingsForSensor(sensorId);

        // Return HTTP 200 OK with readings list
        return Response.ok(history).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Adds a new reading to a specific sensor.
     * 
     * @param reading SensorReading object containing input value
     * @return Response with created reading or error
     */
    @POST
    public Response addReading(SensorReading reading) {

        // Retrieve sensor from data store
        Sensor sensor = store.getSensors().get(sensorId);

        // If sensor not found, return 404 Not Found
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found: " + sensorId + "\"}")
                    .build();
        }

        // Business rule: Sensor must not be in MAINTENANCE state
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }

        // Create a new SensorReading object using provided value
        // (ID and timestamp are usually auto-generated inside the constructor)
        SensorReading newReading = new SensorReading(reading.getValue());

        // Store the new reading in the data store
        store.addReading(sensorId, newReading);

        // Update the current value of the parent sensor
        sensor.setCurrentValue(reading.getValue());

        // Return HTTP 201 Created with location of new reading
        return Response.created(
                URI.create("/api/v1/sensors/" + sensorId + "/readings/" + newReading.getId()))
                .entity(newReading)
                .build();
    }
}