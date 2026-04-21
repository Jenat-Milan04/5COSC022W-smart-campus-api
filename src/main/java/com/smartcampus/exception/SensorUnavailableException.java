package com.smartcampus.exception;

/**
 * Custom exception class used when a sensor is unavailable
 * (e.g., in MAINTENANCE mode) and cannot accept new readings.
 * 
 * This extends RuntimeException, making it an unchecked exception.
 */
public class SensorUnavailableException extends RuntimeException {

    // Stores the ID of the sensor that is unavailable
    private final String sensorId;

    /**
     * Constructor to initialize the exception with a specific sensor ID.
     * 
     * @param sensorId ID of the sensor in maintenance state
     */
    public SensorUnavailableException(String sensorId) {

        // Pass a descriptive message to the parent exception class
        super("Sensor " + sensorId + " is under MAINTENANCE and cannot accept readings.");

        // Assign the sensor ID to the class field
        this.sensorId = sensorId;
    }

    /**
     * Getter method to retrieve the sensor ID associated with the exception.
     * 
     * @return sensorId of the unavailable sensor
     */
    public String getSensorId() {
        return sensorId;
    }
}