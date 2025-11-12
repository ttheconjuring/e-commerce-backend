# 🛍️ Order Service

## 🎯 Purpose

This service is the "aggregate root" for the `Order` entity and the main **entry point** for the entire system. Its primary role is to handle the creation and final state of an order.

## ⚙️ Responsibilities

* Exposes a public REST API for customers to create a new order (`POST /api/orders/create`).
* Exposes a public REST API to check the status of an order (`GET /api/orders/status/{id}`).
* Owns the `Order`, `OrderProduct`, and `Address` database tables.
* Initiates the entire saga by persisting an `Order` and publishing an `OrderCreatedEvent` using the Transactional Outbox pattern.
* Listens for final commands from the orchestrator (e.g., `CompleteOrderCommand`) to update the order's status to a terminal state (`COMPLETED`, `CANCELLED`).

## 📥 Kafka Consumers (Topics it Listens To)

This service listens for final **Commands** from the saga orchestrator:

* `order-commands-topic`: `CompleteOrderCommand`, `CancelOrderCommand`

## 📤 Kafka Producers (Topics it Publishes To)

This service publishes **Events** to the saga orchestrator:

* `order-events-topic`: `OrderCreatedEvent`, `OrderCompletedEvent`, `OrderCancelledEvent`

## 🔌 API Endpoints

* **Create Order:** `POST /api/orders/create`
    * **Body:** `CreateOrderRequest` JSON object.
    * **Response:** `201 CREATED` with `OrderCreatedResponse` body.
* **Check Order Status:** `GET /api/orders/status/{id}`
    * **Response:** `200 OK` with a string representing the status (e.g., "PLACED", "COMPLETED", "CANCELLED").

## 🚀 Getting Started

### Prerequisites

* Java (JDK 17+)
* Docker (or a running instance of Kafka & PostgreSQL)

### Running the Service

1.  **Configure `application.properties`:**
    * Set the Spring Boot server port (e.g., `8080`).
    * Configure the PostgreSQL database connection (for `Order`, `OutboxEvent`, etc.).
    * Configure the Kafka broker connection.
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```