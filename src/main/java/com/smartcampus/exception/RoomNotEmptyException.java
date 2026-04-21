package com.smartcampus.exception;

/**
 * Custom exception class to handle the case when a room
 * cannot be deleted because it still contains sensors.
 * 
 * This extends RuntimeException, meaning it is an unchecked exception.
 */
public class RoomNotEmptyException extends RuntimeException {

    // Stores the ID of the room that caused the exception
    private final String roomId;

    /**
     * Constructor to initialize the exception with a specific room ID.
     * 
     * @param roomId ID of the room that is not empty
     */
    public RoomNotEmptyException(String roomId) {

        // Call parent constructor with a custom error message
        super("Room " + roomId + " still has sensors assigned.");

        // Assign the room ID to the class variable
        this.roomId = roomId;
    }

    /**
     * Getter method to retrieve the room ID associated with the exception.
     * 
     * @return roomId of the non-empty room
     */
    public String getRoomId() {
        return roomId;
    }
}
