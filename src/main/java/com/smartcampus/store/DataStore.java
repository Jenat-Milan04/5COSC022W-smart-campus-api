package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Singleton in-memory data store for Smart Campus system
// Acts like a simple database replacement (no SQL database used here)
public class DataStore {

    // Single instance of DataStore (Singleton pattern)
    private static final DataStore INSTANCE = new DataStore();

    // Thread-safe storage for rooms
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Thread-safe storage for sensors
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Stores sensor readings (time-series data) per sensor ID
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private constructor → prevents external instantiation
    private DataStore() {

        // =========================
        // Seed sample data
        // =========================

        // Create sample rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab", 30);

        // Store rooms in memory
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Create sample sensor
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");

        // Store sensor in memory
        sensors.put(s1.getId(), s1);

        // Link sensor to room
        r1.getSensorIds().add(s1.getId());

        // Initialize empty readings list for sensor
        readings.put(s1.getId(), new ArrayList<>());
    }

    // Returns the single instance of DataStore
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Get all rooms
    public Map<String, Room> getRooms() {
        return rooms;
    }

    // Get all sensors
    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    // Get readings for a specific sensor
    public List<SensorReading> getReadingsForSensor(String sensorId) {

        // If no list exists for sensor, create a new one
        return readings.computeIfAbsent(sensorId, k -> new ArrayList<>());
    }

    // Add a new sensor reading
    public void addReading(String sensorId, SensorReading reading) {
        getReadingsForSensor(sensorId).add(reading);
    }
}