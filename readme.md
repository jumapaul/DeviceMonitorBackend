# NetWatch — Network Device Monitoring Service

## Overview

Build a small Network Device Monitoring Service — a service that manages network infrastructure assets and tracks their
operational status. In a typical telecommunications
or enterprise network, the infrastructure consists of multiple types of equipment including Customer Premises
Equipment (CPEs), routers, access points, optical network terminals (ONTs),
firewalls, and other network devices. These devices continuously report their operational health and availability to
centralized monitoring and management platforms. This assignment focuses
on a simplified version of that problem by providing a system capable of registering devices, receiving status updates,
and presenting their current operational state.

## Problem Statement

Network operations teams need to know, at any point in time:

- What devices are registered in the network?
- What is the current operational status of each device?
- Which devices have stopped reporting — and may therefore be failing?
- What does the recent status history of a specific device look like?

## Technologies Used

- Backend - Spring Boot 3
- Language - Java 17
- Database - PostgreSQL
- Migrations - Flyway
- Messaging - Apache Kafka
- Scheduling - Spring Scheduler
- Frontend - React + TypeScript
- Build Tool - Maven
- Testing - JUnit 5 + Mockito

---

## Architecture

The system uses an **Outbox Pattern** to reliably deliver status reports to Kafka:

```
Client / Device
      │
      ▼
REST API (ReportController)
      │
      ▼
ReportOuterBox (DB table) ◄── acts as a staging area
      │
      ▼
ReportOuterBoxService (Scheduled every 10s)
      │  publishes to Kafka
      ▼
Kafka Topic: device.status
      │
      ├──► ReportService (Kafka Consumer)
      │         saves to report_entity table
      │
      └──► DeviceManagerService
                updates device lastReport + status
```

This pattern ensures that if Kafka is temporarily unavailable, reports are not lost — they sit in the outbox table until
they are successfully published.

---

## Database Schema

### `device_entity`

| Column      | Type      |
|-------------|-----------|
| id          | SERIAL    |
| device_id   | UUID      |
| name        | VARCHAR   |
| type        | VARCHAR   |
| ip_address  | VARCHAR   |
| location    | VARCHAR   |
| status      | VARCHAR   |
| is_stale    | BOOLEAN   |
| created_at  | TIMESTAMP |
| last_report | TIMESTAMP |

### `report_entity`

| Column        | Type      |
|---------------|-----------|
| id            | SERIAL    |
| device_id     | UUID      |
| device_status | VARCHAR   |
| message       | TEXT      |
| created_at    | TIMESTAMP |

### `report_outer_box`

Staging table used by the Outbox Pattern. Records are deleted after successful Kafka acknowledgement.

---

## API Endpoints

### Device Management — `api/v1/device`

#### Register a Device

```
POST /api/v1/device/register
```

Registers a new network device in the system.

**Request body:**

```json
{
  "deviceId": "f475a55e-948f-499c-9261-8524afd5801c",
  "name": "Core-Router-01",
  "type": "ROUTER",
  "ipAddress": "192.168.1.10",
  "location": "Nairobi — Rack B3"
}
```

**Response:**

```json
{
  "message": "Device added successfully",
  "data": {
    "id": 1,
    "deviceId": "f475a55e-948f-499c-9261-8524afd5801c",
    "name": "Core-Router-01",
    "type": "ROUTER",
    "ipAddress": "192.168.1.10",
    "location": "Nairobi — Rack B3",
    "deviceStatus": "ONLINE",
    "isStale": false,
    "createdAt": "2026-06-03T15:41:19",
    "lastReport": null
  }
}
```

---

#### List All Devices

```
GET /api/v1/device
```

Returns all registered devices with their current status, last report timestamp, and stale indicator.

**Response:**

```json
{
  "message": "Devices retrieved successfully",
  "data": [
    {
      "id": 1,
      "deviceId": "f475a55e-948f-499c-9261-8524afd5801c",
      "name": "Core-Router-01",
      "type": "ROUTER",
      "ipAddress": "192.168.1.10",
      "location": "Nairobi",
      "deviceStatus": "ONLINE",
      "isStale": false,
      "createdAt": "2026-06-03T15:41:19",
      "lastReport": "2026-06-03T22:36:36"
    }
  ]
}
```

---

#### Update a Device

```
PUT /api/v1/device/update
```

Updates the IP address and/or location of an existing device.

**Request body:**

```json
{
  "id": 1,
  "ipAddress": "10.0.0.1",
  "location": "Mombasa — Rack A1"
}
```

---

### Status Reports — `api/v1/report`

#### Submit a Status Report

```
POST /api/v1/report/submit
```

Submits a status report for a registered device. The report is staged in the outbox and published to Kafka
asynchronously.

**Request body:**

```json
{
  "deviceId": "f475a55e-948f-499c-9261-8524afd5801c",
  "deviceStatus": "DEGRADED",
  "message": "Packet loss detected — 8% drop rate"
}
```

**Response:**

```json
{
  "message": "Success",
  "data": "Report sent"
}
```

---

#### Get Device Report History

```
GET /api/v1/report/{deviceId}?page=0&size=20
```

Returns a paginated list of the device's most recent status reports alongside its current metadata.

**Path parameter:** `deviceId` — the UUID of the device

**Query parameters:**
| Parameter | Default | Description |
|---|---|---|
| page | 0 | Page number (zero-indexed) |
| size | 20 | Number of reports per page |

**Response:**

```json
{
  "message": "Success",
  "data": {
    "deviceInfoResponse": {},
    "reports": [],
    "totalElements": 15,
    "totalPages": 1,
    "hasNext": false
  }
}
```

---

## Stale Device Detection

A device is considered **stale** if it has not submitted a status report within the last **15 minutes**. A Spring
`@Scheduled` job runs every 60 seconds and:

1. Queries for all `ONLINE` devices whose `lastReport` is older than 15 minutes
2. Sets their status to `OFFLINE` and `isStale` to `true`
3. Logs a warning for each stale device

This ensures operators are always aware of devices that may have silently failed.

---

## Assumptions Made

- **Device identity is managed externally** — the `deviceId` UUID is supplied by the caller at registration rather than
  generated by the backend. This assumes devices have a pre-assigned identifier.
- **Single service architecture** — device management and reporting are handled within a single Spring Boot application
  rather than separate microservices. This was a pragmatic choice for the scope of the assignment.
- **Kafka is available locally** — the system assumes a locally running Kafka instance on the default port `9092`.
- **Status reports are periodic** — the system assumes devices report regularly. There is no mechanism to force a device
  to report on demand.
- **Stale threshold is fixed at 15 minutes** — this is hardcoded and not configurable via environment variable or
  properties file.
- **No authentication** — Authentication is handled by a separate service.
- **Report history is append-only** — reports are never updated or deleted (except from the outbox after Kafka
  acknowledgement).

---

## Future Improvements

- **Firebase Cloud Messaging (FCM) notifications** — send push notifications to operators when a device goes stale,
  rather than only logging a warning. A `device_tokens` table would store operator FCM tokens.
- **Redis caching** — cache stable device metadata (name, type, location) to reduce repeated database reads. Status
  fields would be updated via the Kafka consumer rather than cache eviction.
- **WebSocket or SSE dashboard** — push real-time device status changes to the React frontend rather than polling every
  30 seconds.

## How to set up and run

- Clone the project from git

```
git clone git@github.com:jumapaul/DeviceMonitorBackend.git
```

- Start the docker compose to pull the images.

```
docker compose up -d
```
- Pass the Database username and password as environment files with the name `POSTGRES_USERNAME` and `POSTGRES_PASSWORD` respectively.

- Run the application.