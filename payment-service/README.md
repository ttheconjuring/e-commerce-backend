# 💳 Payment Service

## 🎯 Purpose

This service is a "worker" responsible for a single business capability: **processing payments**. It is a fully independent service that knows nothing about orders, products, or shipping—it only knows how to process a payment.

## ⚙️ Responsibilities

* Listens for a `ProcessPaymentCommand` from the saga orchestrator.
* Simulates (or, in a real system, calls an external payment gateway like Stripe).
* Saves a record of the transaction to its own `Payment` database table.
* Publishes the outcome (`PaymentSucceededEvent` or `PaymentFailedEvent`) using the Transactional Outbox pattern.

## 📥 Kafka Consumers (Topics it Listens To)

This service listens for **Commands** from the saga orchestrator:

* `payment-commands-topic`: `ProcessPaymentCommand`

## 📤 Kafka Producers (Topics it Publishes To)

This service publishes **Events** to the saga orchestrator:

* `payment-events-topic`: `PaymentSucceededEvent`, `PaymentFailedEvent`

## 🔌 API Endpoints

This service has **no public API**. It is a purely event-driven backend component.

## 🚀 Getting Started

### Prerequisites

* Java (JDK 17+)
* Docker (or a running instance of Kafka & PostgreSQL)

### Running the Service

1.  **Configure `application.properties`:**
    * Set the Spring Boot server port.
    * Configure the PostgreSQL database connection (for `Payment`, `OutboxEvent`).
    * Configure the Kafka broker connection.
2.  **Run the application:**
    ```bash
    ./gradlew bootRun
    ```