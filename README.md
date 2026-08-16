# FinStream Data Ingestion Service

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.3.2](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green.svg)](https://www.mongodb.com/)
[![HDFS](https://img.shields.io/badge/Apache%20Hadoop-HDFS%203.3.6-blue.svg)](https://hadoop.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Multi--stage-blue.svg)](https://www.docker.com/)
[![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0-blueviolet.svg)](https://swagger.io/)

A high-performance, resilient Spring Boot microservice for ingesting financial statement files (PDF, CSV, JSON), persisting file streams to Apache HDFS, managing document metadata in MongoDB, and tracking processing state across external Spark / Airflow data pipelines.

---

## Architecture Overview

```
                          +-------------------------------+
                          |     Client File Upload        |
                          +---------------+---------------+
                                          |
                                          v
                    +-------------------------------------------+
                    |    FinStream Ingestion Microservice       |
                    |            (Spring Boot 3)                |
                    +------+-----------------------------+------+
                           |                             |
                 File Stream (HDFS)              Metadata (MongoDB)
                           |                             |
                           v                             v
                   +---------------+             +---------------+
                   |  Apache HDFS  |             | MongoDB Atlas |
                   |  File Storage |             | Document Store|
                   +---------------+             +---------------+
                           |                             ^
                           | Read Stream                 | Status PATCH
                           v                             |
                   +-------------------------------------+-------+
                   |     External Spark / Airflow Pipeline       |
                   |    (Decoupled Event Driven Processing)      |
                   +---------------------------------------------+
```

---

## Key Features

### 1. Robust File Ingestion & HDFS Streaming
- Streams uploaded financial statements (PDF, CSV, JSON) directly to Apache HDFS under `/finance/uploads/{source}/{file_type}/`.
- Automatic orphan file cleanup: If MongoDB metadata persistence fails, any partially written HDFS files are automatically purged to prevent orphaned data.

### 2. Decoupled Spark / Airflow Processing Workflow
- Upon upload, `ProcessingMetadata` is automatically created with initial status **`UPLOADED`**.
- Processing is decoupled: external Spark / Airflow pipelines pick up files independently and advance state by invoking `PATCH /v1/files/{file_id}/status`.
- Lifecycle status transitions: `UPLOADED` $\rightarrow$ `PROCESSING` $\rightarrow$ `PROCESSED` (or `PROCESSING_FAILED`).

### 3. Event-Driven Audit Notifications
- Emits `FileUploadEvent` asynchronously via Spring `@Async` `@EventListener` for decoupled logging, auditing, and downstream integration.

### 4. Database Indexing & Query Optimization
- Unique indexing on `fileId` in MongoDB (`@Indexed(unique = true)`).
- Single-query lookups for status queries ($O(1)$ resolution by `fileId` or `documentId`).

### 5. Audit Timestamps & Modern Date/Time API
- Uses `java.time.Instant` across all entities (`FileMetadata` and `ProcessingMetadata`).
- Spring Mongo Auditing enabled (`@EnableMongoAuditing`) with `@CreatedDate` and `@LastModifiedDate`.

### 6. Type-Safe Configuration Properties
- Strongly typed property binding via `@ConfigurationProperties(prefix = "hdfs")` for `hdfs.uri` and `hdfs.upload.path`.

### 7. Health & Monitoring Endpoints
- Spring Boot Actuator health endpoint at `/v1/actuator/health`.
- Custom `HdfsHealthIndicator` reporting HDFS filesystem status.

### 8. Interactive OpenAPI 3.0 & Swagger UI
- Self-documenting REST API with embedded Swagger UI at `/v1/swagger-ui.html`.

---

## API Reference

### 1. File Upload API

#### Upload Financial Statement
```http
POST /v1/uploads/{file_source}/{file_type}/
Content-Type: multipart/form-data
```
- **Path Parameters**:
  - `file_source`: `hdfc`, `sbi`, `paytm`, `payzap`
  - `file_type`: `bankstatement`, `upiservice`, `wallet`
- **Form Data**:
  - `file`: Multipart file payload
- **Response**: `200 OK` returning `FileMetadata` document.

---

### 2. File Metadata & Status API

#### Get All Files
```http
GET /v1/files
```
Returns list of all `FileMetadata` records.

#### Get File Metadata by ID
```http
GET /v1/files/{file_id}
```
Fetches metadata by file UUID or MongoDB Document ID.

#### Get Processing Status by File ID
```http
GET /v1/files/{file_id}/status
```
Returns `ProcessingMetadata` record (status, timestamps, document references).

#### Get Files by Processing Status
```http
GET /v1/files/status/{status}
```
Filter files by status (`UPLOADED`, `PROCESSING`, `PROCESSED`, `PROCESSING_FAILED`).

#### Update Processing Status (Used by Spark / Airflow)
```http
PATCH /v1/files/{file_id}/status
Content-Type: application/json

{
  "processingStatus": "PROCESSING"
}
```
Updates processing status and automatically updates `endDateTime` when completed or failed.

---

## Interactive Swagger & Actuator Endpoints

When running locally:
- **Swagger UI**: [http://localhost:8080/v1/swagger-ui.html](http://localhost:8080/v1/swagger-ui.html)
- **OpenAPI JSON Spec**: [http://localhost:8080/v1/v3/api-docs](http://localhost:8080/v1/v3/api-docs)
- **Health Check**: [http://localhost:8080/v1/actuator/health](http://localhost:8080/v1/actuator/health)

---

## Containerization & Deployment

### Run with Docker Compose
To spin up the entire application stack including MongoDB locally:

```bash
docker-compose up --build -d
```

To stop containers:
```bash
docker-compose down
```

### Build Docker Image Manually
```bash
docker build -t finstream-data-ingestion:latest .
```

Run container with environment variables:
```bash
docker run -d \
  -p 8080:8080 \
  -e MONGO_URI="mongodb+srv://user:pass@cluster.mongodb.net/finstream_database" \
  -e HDFS_URI="hdfs://localhost:9000" \
  --name finstream-ingestion \
  finstream-data-ingestion:latest
```

---

## Getting Started & Build Instructions

### Prerequisites
- **Java 17** or higher
- **Maven 3.8+**
- **MongoDB** (Atlas or local container)
- **Hadoop HDFS** (optional for local testing with fallback)

### Build and Run Tests
```bash
mvn clean install
```

### Run Service Locally
```bash
mvn spring-boot:run
```
Service will start on port `8080` with base path `/v1/`.

---

## Tech Stack Summary

- **Framework**: Spring Boot 3.3.2 (Actuator, Web, Mongo)
- **Persistence**: Spring Data MongoDB, Apache Hadoop HDFS Client 3.3.6
- **Documentation**: SpringDoc OpenAPI 2.5.0 / Swagger UI
- **Containerization**: Docker Multi-stage Build, Docker Compose
- **Build Tool**: Apache Maven
