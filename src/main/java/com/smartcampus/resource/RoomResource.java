package com.smartcampus.resource;

// Import custom exception for business rule validation
import com.smartcampus.exception.RoomNotEmptyException;

// Import Room model representing room entity
import com.smartcampus.model.Room;

// Import in-memory data storage (Singleton pattern)
import com.smartcampus.store.DataStore;

// Import JAX-RS annotations and classes for RESTful API
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

// Import for creating URI for newly created resources
import java.net.URI;

// Import collection interface for handling multiple rooms
import java.util.Collection;

/**
 * RoomResource class provides RESTful endpoints for managing Room entities.
 * It supports CRUD operations such as retrieving, creating, and deleting rooms.
 */
@Path("/rooms") // Base URI path for room-related operations
@Produces(MediaType.APPLICATION_JSON) // All responses are in JSON format
@Consumes(MediaType.APPLICATION_JSON) // Accepts JSON input
public class RoomResource {

    // Singleton instance of DataStore to manage application data
    private final DataStore store = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Retrieves all rooms from the data store.
     * 
     * @return Response containing list of all rooms
     */
    @GET
    public Response getAllRooms() {
        // Fetch all room objects from the data store
        Collection<Room> rooms = store.getRooms().values();

        // Return HTTP 200 OK with room list
        return Response.ok(rooms).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room.
     * 
     * @param room Room object received from client
     * @return Response with created room or error message
     */
    @POST
    public Response createRoom(Room room) {

        // Validate if Room ID is provided
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST) // 400 Bad Request
                    .entity("{\"error\":\"Room ID is required\"}")
                    .build();
        }

        // Check if room with same ID already exists
        if (store.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT) // 409 Conflict
                    .entity("{\"error\":\"Room with this ID already exists\"}")
                    .build();
        }

        // Add new room to the data store
        store.getRooms().put(room.getId(), room);

        // Return HTTP 201 Created with location header and created room object
        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room)
                .build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Retrieves a specific room by its ID.
     * 
     * @param roomId ID of the room
     * @return Response with room data or error if not found
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        // Retrieve room from data store
        Room room = store.getRooms().get(roomId);

        // If room does not exist, return 404 Not Found
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found: " + roomId + "\"}")
                    .build();
        }

        // Return HTTP 200 OK with room data
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room if it exists and has no associated sensors.
     * 
     * @param roomId ID of the room to delete
     * @return Response indicating success or failure
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        // Retrieve room from data store
        Room room = store.getRooms().get(roomId);

        // If room not found, return 404 Not Found
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Room not found: " + roomId + "\"}")
                    .build();
        }

        // Business rule: Cannot delete room if it contains sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId); // Custom exception handling
        }

        // Remove room from data store
        store.getRooms().remove(roomId);

        // Return HTTP 204 No Content (successful deletion with no body)
        return Response.noContent().build();
    }
}