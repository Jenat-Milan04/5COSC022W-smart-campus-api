package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8080/api/v1/";  // Base URL where the server will run

    public static void main(String[] args) throws Exception {
        // Create a Jersey ResourceConfig
        // This tells Jersey to scan the package "com.smartcampus"
        // for REST API classes (like @Path annotated resources)
        ResourceConfig config = new ResourceConfig().packages("com.smartcampus");

        // Create and start the Grizzly HTTP server
        // It uses the BASE_URI and the Jersey configuration
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
            URI.create(BASE_URI), config
        );
        System.out.println("Smart Campus API running at " + BASE_URI);
        System.out.println("Press ENTER to stop...");
        System.in.read();
        server.shutdownNow();
    }
}

