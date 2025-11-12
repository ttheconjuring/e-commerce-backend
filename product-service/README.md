# 📦 Product Service

## 🎯 Purpose

This service is a "worker" responsible for a single business capability: **managing inventory**. It is the source of truth for product information and stock levels.

## ⚙️ Responsibilities

* Owns the `Product` and `Category` database tables.
* Listens for a `ConfirmAvailabilityCommand` and checks its local `Product` table for stock levels.
* Listens for an `UpdateProductsCommand` to permanently decrement stock (the "commit" step after payment).
* Publishes the outcome (`AvailabilityConfirmedEvent` or `ProductsShortageEvent`) using the Transactional Outbox pattern.

## 📥 Kafka Consumers (Topics it Listens To)

This service listens for **Commands** from the saga orchestrator:

* `product-commands-topic`: `ConfirmAvailabilityCommand`, `UpdateProductsCommand`

## 📤 Kafka Producers (Topics it Publishes To)

This service publishes **Events** to the saga orchestrator:

* `product-events-topic`: `AvailabilityConfirmedEvent`, `ProductsShortageEvent`, `ProductsUpdatedEvent`

## 🔌 API Endpoints

This service has **no public API**. It is a purely event-driven backend component. (A `POST /seed` endpoint could be added for testing).

## 🚀 Getting Started

### Prerequisites

* Java (JDK 17+)
* Docker (or a running instance of Kafka & PostgreSQL)

### Running the Service

1.  **Configure `application.properties`:**
    * Set the Spring Boot server port.
    * Configure the PostgreSQL database connection (for `Product`, `OutboxEvent`).
    * Configure the Kafka broker connection.
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```