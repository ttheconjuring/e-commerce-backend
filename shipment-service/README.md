# 🚚 Shipment Service

## 🎯 Purpose

This service is a "worker" responsible for a single business capability: **managing shipments**. It integrates with (or simulates) external shipping carriers.

## ⚙️ Responsibilities

* Owns the `Shipment` database table.
* Listens for an `ArrangeShipmentCommand` to book a shipment with a carrier.
* Listens for a `CancelShipmentCommand` (a compensating transaction) to roll back a shipment if the saga fails (e.g., payment failure).
* Publishes the outcome (`ShipmentArrangedEvent`, `ArrangementFailedEvent`, `ShipmentCancelledEvent`) using the Transactional Outbox pattern.

## 📥 Kafka Consumers (Topics it Listens To)

This service listens for **Commands** from the saga orchestrator:

* `shipment-commands-topic`: `ArrangeShipmentCommand`, `CancelShipmentCommand`

## 📤 Kafka Producers (Topics it Publishes To)

This service publishes **Events** to the saga orchestrator:

* `shipment-events-topic`: `ShipmentArrangedEvent`, `ArrangementFailedEvent`, `ShipmentCancelledEvent`

## 🔌 API Endpoints

This service has **no public API**. It is a purely event-driven backend component.

## 🚀 Getting Started

### Prerequisites

* Java (JDK 17+)
* Docker (or a running instance of Kafka & PostgreSQL)

### Running the Service

1.  **Configure `application.properties`:**
    * Set the Spring Boot server port.
    * Configure the PostgreSQL database connection (for `Shipment`, `OutboxEvent`).
    * Configure the Kafka broker connection.
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```