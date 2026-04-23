# 5COSC022W-smart-campus-api
RESTful Smart Campus Sensor &amp; Room Management API built with JAX-RS (Jersey) for the 5COSC022W Client-Server Architectures coursework.

**Module:** 5COSC022W — Client-Server Architectures  
**Student:** Jenat Milan Jeyachandran 
**Student ID:** 202420622 / w2152922
**GitHub Repo:** https://github.com/Jenat-Milan04/5COSC022W-smart-campus-api

---

## Overview

This project is a fully RESTful API built using **JAX-RS (Jersey 2.41)** and an embedded **Grizzly HTTP server**. It simulates a real-world Smart Campus infrastructure where facilities managers can manage Rooms, Sensors, and historical Sensor Readings through a clean, versioned REST interface.

The API is structured around three core resources:
- **Rooms** — physical spaces on campus (e.g., labs, libraries)
- **Sensors** — devices deployed inside rooms (e.g., CO2, Temperature, Occupancy)
- **Sensor Readings** — historical log of measurements recorded by each sensor

All data is stored in-memory using `ConcurrentHashMap` and `ArrayList`. No database is used.