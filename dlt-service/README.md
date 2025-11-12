# 🪦 Dead-Letter Topic (DLT) Service

## 🎯 Purpose

This service is the central **"safety net"** and observability tool for the entire microservice ecosystem. Its sole purpose is to **consume, log, and expose** any message that has failed processing in any of the other services.

When a service (like `payment-service`) fails to process a command after all retries, the Kafka error handler routes the "poison pill" message to a **Dead-Letter Topic** (DLT), such as `payment-commands-topic.DLT`. This service listens to all DLTs.

## ⚙️ Responsibilities

* Listens to all DLTs across the platform (e.g., `*-dlt`).
* Uses a multi-method `@KafkaHandler` to deserialize any type of failed message.
* Saves the failed message's payload and metadata to the `DltMessage` database table.
* Exposes a REST API for administrators to view the failed messages and diagnose problems.

## 📥 Kafka Consumers (Topics it Listens To)

This service listens to **ALL Dead-Letter Topics**:

* `order-events-topic-dlt`
* `order-commands-topic-dlt`
* `payment-events-topic-dlt`
* `payment-commands-topic-dlt`
* `product-events-topic-dlt`
* `product-commands-topic-dlt`
* `shipment-events-topic-dlt`
* `shipment-commands-topic-dlt`

## 📤 Kafka Producers (Topics it Publishes To)

This service is a **terminal consumer** and does not publish any messages.

## 🔌 API Endpoints

* **List All Errors:** `GET /error/check`
    * **Response:** `200 OK` with a JSON array of `DltMessageDTO` objects.

## 🚀 Getting Started

### Prerequisites

* Java (JDK 17+)
* Docker (or a running instance of Kafka & PostgreSQL)

### Running the Service

1.  **Configure `application.properties`:**
    * Set the Spring Boot server port.
    * Configure the PostgreSQL database connection (for `DltMessage`).
    * Configure the Kafka broker connection.
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```