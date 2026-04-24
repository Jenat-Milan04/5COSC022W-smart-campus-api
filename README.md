# 5COSC022W-smart-campus-api
RESTful Smart Campus Sensor &amp; Room Management API built with JAX-RS (Jersey) for the 5COSC022W Client-Server Architectures coursework.

**Module:** 5COSC022W — Client-Server Architectures  
**Student:** Jenat Milan Jeyachandran 
**Student ID:** 20242062 / w2152922
**GitHub Repo:** https://github.com/Jenat-Milan04/5COSC022W-smart-campus-api

---

## Overview

This project is a fully RESTful API built using **JAX-RS (Jersey 2.41)** and an embedded **Grizzly HTTP server**. It simulates a real-world Smart Campus infrastructure where facilities managers can manage Rooms, Sensors, and historical Sensor Readings through a clean, versioned REST interface.

The API is structured around three core resources:
- **Rooms** — physical spaces on campus (e.g., labs, libraries)
- **Sensors** — devices deployed inside rooms (e.g., CO2, Temperature, Occupancy)
- **Sensor Readings** — historical log of measurements recorded by each sensor

All data is stored in-memory using `ConcurrentHashMap` and `ArrayList`. No database is used.

---

---

# Conceptual Report — Question Answers

---

## Part 1: Service Architecture & Setup

### Question 1.1 — JAX-RS Resource Lifecycle and Impact on In-Memory Data

By default in JAX-RS, a new instance of each resource class is created for **every single incoming HTTP request**. This is called the **request-scoped** lifecycle. So if ten clients call `GET /api/v1/rooms` at the same time, the JAX-RS runtime (Jersey in our case) creates ten separate `RoomResource` objects — one for each request — processes them, then discards them all.

This has a direct consequence for how shared data must be managed. If each resource instance held its own copy of the rooms or sensors data, every request would start with a blank slate and data created in one request would vanish immediately after. To solve this, the project uses a **Singleton DataStore** — a single shared object (`DataStore.getInstance()`) that lives for the entire lifetime of the server. All resource instances, regardless of how many are created per second, point to the same `DataStore` and operate on the same underlying maps.

However, because multiple requests can run concurrently (on different threads), using a regular `HashMap` would be dangerous. If two requests try to write to the map at the same time, the internal state of the map can become corrupted — this is a classic **race condition**. To prevent this, the project uses `ConcurrentHashMap` for all three data collections (rooms, sensors, readings). `ConcurrentHashMap` is a thread-safe variant of `HashMap` that allows concurrent reads and handles concurrent writes safely by locking only the affected segment of the map rather than the entire structure. This means the API can serve many simultaneous requests without risking data corruption or loss.

---

### Question 1.2 — What is HATEOAS and Why Does It Matter?

HATEOAS stands for **Hypermedia As The Engine Of Application State**. The idea is simple but powerful: instead of just returning raw data, the API also returns links telling the client what it can do next and where to go. For example, our `GET /api/v1` discovery endpoint returns not just the API version and contact info, but also a `_links` block that explicitly tells callers where the rooms and sensors collections are located.

The key benefit of this approach is that **clients become self-navigating**. A developer consuming the API doesn't need to memorise or hard-code any URLs. They start at the root endpoint, read the links, and follow them. If the API structure changes in the future — say, rooms moves from `/api/v1/rooms` to `/api/v2/campus/rooms` — clients that navigate via links will adapt automatically, while clients that hard-coded the old URL will break.

Compared to static documentation, HATEOAS is always up to date by definition, because the links come directly from the live server. Documentation can go stale, be incorrect, or simply not be read by developers. With hypermedia, the API essentially documents itself at runtime, making it far more resilient and developer-friendly in the long run.

---

## Part 2: Room Management

### Question 2.1 — Returning Full Room Objects vs. Returning Only IDs

When designing a `GET /api/v1/rooms` endpoint, there are two common approaches: return a list of just the room IDs (e.g., `["LIB-301", "LAB-101"]`), or return the full room objects with all fields (id, name, capacity, sensorIds, etc.).

Returning **only IDs** reduces the size of the initial response significantly. This is useful if the client only needs to display a list of room names and will fetch details lazily. However, it means the client has to make a second request (`GET /api/v1/rooms/{id}`) for every room it wants to display, which creates what is known as the **N+1 problem** — one request to get the list, then N more requests for N rooms. This can be very slow and puts unnecessary load on both the network and the server.

Returning **full room objects** in the list response increases the payload size, but means the client gets everything it needs in a single request. For a campus system where a manager might want to see all rooms and their sensor counts at a glance, this is far more practical. The current implementation uses full objects precisely because it makes the client's job simpler and eliminates multiple round trips. The trade-off is acceptable given that room objects are relatively lightweight — they contain just a few fields and a list of sensor IDs.

---

### Question 2.2 — Is the DELETE Operation Idempotent?

In REST, an operation is considered **idempotent** if sending the exact same request multiple times produces the same final server state as sending it once.

In this implementation, DELETE **is idempotent in terms of the server's data state**, but it is not uniform in terms of the HTTP response code. Here is what happens across multiple identical DELETE calls on the same room ID:

- **First call:** The room exists, has no sensors, gets deleted. Server returns `204 No Content`.
- **Second call:** The room no longer exists. Server returns `404 Not Found`.
- **Third, fourth calls onward:** Same as the second — `404 Not Found`.

After the first successful deletion, the room is gone. Every subsequent call finds the same empty state — the room is still gone. The server's data is never changed by the second or third call. From a **data integrity standpoint**, the operation is idempotent because repeated calls do not cause any additional side effects or corruption.

The HTTP response code does change from `204` to `404`, but this is expected and correct behaviour. The important thing is that no unwanted deletions, duplicate operations, or data inconsistencies occur. A client can safely retry a DELETE request if it's uncertain whether the first one succeeded, without worrying about accidentally deleting something else or causing an error in the system.

---

## Part 3: Sensor Operations & Linking

### Question 3.1 — Consequences of Sending the Wrong Content-Type to a @Consumes Endpoint

The `POST /api/v1/sensors` endpoint is annotated with `@Consumes(MediaType.APPLICATION_JSON)`. This tells JAX-RS that this method will only accept requests where the `Content-Type` header is `application/json`.

If a client sends data with a different content type — say `text/plain` or `application/xml` — JAX-RS will **reject the request before it even reaches the method body**. It returns an **HTTP 415 Unsupported Media Type** response automatically. No custom code is needed for this; it is handled entirely by the framework.

This is a really important safety feature. Without the `@Consumes` constraint, JAX-RS might still attempt to deserialise the request body and fail midway with a confusing internal error. By declaring the expected media type upfront, the API clearly communicates its contract: "I only speak JSON." A client sending XML will get an immediate, unambiguous `415` response telling them exactly what went wrong, rather than a cryptic `500` error. This makes the API much easier to debug and integrate with.

---

### Question 3.2 — Query Parameter vs. Path Parameter for Filtering

The filtering endpoint is designed as `GET /api/v1/sensors?type=CO2` using a `@QueryParam`. An alternative design would be `GET /api/v1/sensors/type/CO2` using a path segment. While both work, the query parameter approach is considered best practice for filtering, and here is why.

In REST architecture, the **path** is used to identify a specific resource or collection. `/api/v1/sensors` identifies the sensors collection. `/api/v1/sensors/TEMP-001` identifies a specific sensor. If we add `/type/CO2` to the path, we are implying that `type` is a sub-resource under sensors, which is semantically wrong — CO2 is not a resource, it's a filter criterion.

**Query parameters**, on the other hand, are designed exactly for this purpose — they modify or narrow the results of a request without changing the identity of the resource being accessed. `GET /api/v1/sensors?type=CO2` still refers to the sensors collection, just with a filter applied. This also makes it trivially easy to add more filters in the future (`?type=CO2&status=ACTIVE`) without restructuring the URL hierarchy. Path-based filtering would require new route definitions for every combination, which quickly becomes unmanageable. Query parameters are also optional by nature, so the same `GET /api/v1/sensors` endpoint works both with and without the filter, which is clean and flexible.

---

## Part 4: Deep Nesting with Sub-Resources

### Question 4.1 — Architectural Benefits of the Sub-Resource Locator Pattern

In the `SensorResource` class, rather than defining the readings endpoints directly, there is a sub-resource locator method:

```java
@Path("/{sensorId}/readings")
public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
    return new SensorReadingResource(sensorId);
}
```

This delegates all `/readings` logic to a completely separate `SensorReadingResource` class. The architectural benefits of this are significant.

First, it keeps each class **focused and manageable**. `SensorResource` handles sensor concerns. `SensorReadingResource` handles reading concerns. Neither class has to know everything about the other. If requirements change — say, readings need pagination or a new filter — only `SensorReadingResource` needs to be modified.

Second, it **reflects the physical resource hierarchy** of the system. Readings genuinely belong to a specific sensor, so it makes sense for the readings resource to be nested under sensors in the URL. The sub-resource locator pattern makes this nesting explicit in the code structure, not just in the URL.

Third, as the API grows, if someone needs to understand how readings work, they go directly to `SensorReadingResource`. They don't have to wade through hundreds of lines in a single monolithic controller to find what they're looking for. In large APIs with dozens of nested resources, having everything in one class is a maintenance nightmare. Delegation to separate classes is how real-world production APIs stay manageable over time.

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### Question 5.1 — Why HTTP 422 is More Accurate Than 404 for a Missing roomId Reference

When a client tries to register a sensor with a `roomId` that doesn't exist in the system, this project throws a `LinkedResourceNotFoundException` and returns **HTTP 422 Unprocessable Entity**.

At first, a `404 Not Found` might seem like the right choice because the room "wasn't found." But 404 has a very specific meaning in HTTP: the resource at the **requested URL** does not exist. In this scenario, the URL `/api/v1/sensors` is perfectly valid — it exists and is reachable. The problem isn't the URL; the problem is that a **value inside the JSON body** references something that doesn't exist.

HTTP 422 was designed precisely for this situation. It means: "I understood the request, I parsed the JSON, but the data inside it is semantically invalid." The request body was well-formed JSON, but its contents couldn't be processed because they reference a non-existent entity. Returning 422 gives the client a much clearer signal — "your JSON is valid but your data is wrong" rather than "the URL you called doesn't exist." This distinction is crucial for client developers debugging integration issues, as it immediately tells them the problem is in their payload, not their URL.

---

### Question 5.2 — Cybersecurity Risks of Exposing Java Stack Traces

Without a `GlobalExceptionMapper`, an unexpected runtime error in a JAX-RS application can result in a raw Java stack trace being returned to the caller in the HTTP response body. This is a serious security risk for several reasons.

A stack trace reveals the **internal package and class structure** of the application (e.g., `com.smartcampus.resource.SensorResource`). An attacker now knows the exact technology stack, class names, and code organisation. It often exposes **library names and version numbers** (e.g., `org.glassfish.jersey` version `2.41`). Attackers can look up known CVEs (Common Vulnerabilities and Exposures) for that exact version and craft targeted exploits. The line numbers in a stack trace can even hint at **specific logic flaws or edge cases** in the code that could be probed further.

In short, a stack trace is a roadmap of your application's internals handed directly to a potential attacker. The `GlobalExceptionMapper<Throwable>` in this project catches all unexpected exceptions and returns a clean, generic `500 Internal Server Error` with a safe message. The real error is logged server-side where only authorised developers can see it, while the external caller gets no useful information for crafting an attack. This follows the principle of **minimum information disclosure** in secure API design.

---

### Question 5.3 — Why Use JAX-RS Filters for Logging Instead of Manual Logger Calls

The `LoggingFilter` class implements both `ContainerRequestFilter` and `ContainerResponseFilter`, which means every single HTTP request and response passes through it automatically — no changes needed in the resource classes.

If logging were done manually instead, you would need to add `Logger.info()` calls at the start and end of every single resource method. This creates several problems. First, it is **repetitive and error-prone** — it is easy to forget to add logging to a new method, leading to gaps in observability. Second, it **clutters the business logic** with infrastructure concerns. A method that should be focused on creating a room now also has to worry about logging — these are two completely different responsibilities mixed together. This violates the **Single Responsibility Principle**.

Filters, by contrast, are a perfect example of handling **cross-cutting concerns** — behaviour that needs to apply everywhere but doesn't belong to any specific business method. By implementing it once in `LoggingFilter`, the logging is guaranteed to apply to every endpoint, present and future, without touching a single resource class. If the log format ever needs to change, there is exactly one place to update it. This is cleaner, more maintainable, and reflects professional-grade API design.

---

## Project Structure

```
smart-campus-api/
├── pom.xml
└── src/main/java/com/smartcampus/
    ├── Main.java
    ├── App.java
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    ├── store/
    │   └── DataStore.java
    ├── resource/
    │   ├── DiscoveryResource.java
    │   ├── RoomResource.java
    │   ├── SensorResource.java
    │   └── SensorReadingResource.java
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── LinkedResourceNotFoundException.java
    │   ├── SensorUnavailableException.java
    │   ├── RoomNotEmptyExceptionMapper.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   ├── SensorUnavailableExceptionMapper.java
    │   └── GlobalExceptionMapper.java
    └── filter/
        └── LoggingFilter.java
```

---

## How to Build and Run

**Prerequisites:** Java 11+, Maven 3.6+

**Step 1 — Clone the repository**
```bash
git clone https://github.com/Jenat-Milan04/5COSC022W-smart-campus-api.git
cd smart-campus-api
```

**Step 2 — Build the project**
```bash
mvn clean package
```

**Step 3 — Run the server**
```bash
java -jar target/smart-campus-api-1.0-SNAPSHOT.jar
```

**Step 4 — The API is now live at:**
```
http://localhost:8080/api/v1
```

Press `ENTER` in the terminal to stop the server.

---

## API Endpoints Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1` | Discovery — API metadata and links |
| GET | `/api/v1/rooms` | List all rooms |
| POST | `/api/v1/rooms` | Create a new room |
| GET | `/api/v1/rooms/{roomId}` | Get a specific room |
| DELETE | `/api/v1/rooms/{roomId}` | Delete a room (blocked if sensors exist) |
| GET | `/api/v1/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/api/v1/sensors` | Register a new sensor |
| GET | `/api/v1/sensors/{sensorId}` | Get a specific sensor |
| GET | `/api/v1/sensors/{sensorId}/readings` | Get all readings for a sensor |
| POST | `/api/v1/sensors/{sensorId}/readings` | Add a new reading for a sensor |

---

## Sample curl Commands

**1. Hit the Discovery endpoint**
```bash
curl http://localhost:8080/api/v1
```

**2. Create a new Room**
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"CS-101","name":"Computer Science Lab","capacity":40}'
```

**3. Register a new Sensor (linked to the room above)**
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":400,"roomId":"CS-101"}'
```

**4. Filter sensors by type**
```bash
curl "http://localhost:8080/api/v1/sensors?type=CO2"
```

**5. Post a new sensor reading**
```bash
curl -X POST http://localhost:8080/api/v1/sensors/CO2-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":450.5}'
```

**6. Attempt to delete a room that still has sensors (triggers 409 Conflict)**
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

**7. Attempt to register a sensor with a non-existent roomId (triggers 422)**
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0,"roomId":"FAKE-999"}'
```

---
