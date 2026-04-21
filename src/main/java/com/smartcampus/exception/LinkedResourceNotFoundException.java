package com.smartcampus.exception;

/**
 * Custom exception class used when a linked resource (e.g., a Room)
 * does not exist in the system.
 * 
 * This is typically thrown when creating a resource (like a Sensor)
 * that references a non-existing Room.
 * 
 * It extends RuntimeException, so it is an unchecked exception.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    // Stores the ID of the missing linked resource (Room)
    private final String roomId;

    /**
     * Constructor to initialize the exception with the missing room ID.
     * 
     * @param roomId ID of the room that was not found
     */
    public LinkedResourceNotFoundException(String roomId) {

        // Pass a descriptive error message to the parent class
        super("Room with ID '" + roomId + "' does not exist.");

        // Assign the room ID to the class field
        this.roomId = roomId;
    }

    /**
     * Getter method to retrieve the missing room ID.
     * 
     * @return roomId that caused the exception
     */
    public String getRoomId() {
        return roomId;
    }
}