# 🚀 Order Saga Orchestrator Service

## 🎯 Purpose

This service is the **central brain** of the entire distributed transaction (Saga). It does not contain any core
business logic itself (like processing a payment or checking stock). Instead, its sole responsibility is to:

1. **Listen** for events from all other microservices (e.g., `OrderCreated`, `PaymentSucceeded`, `ProductsShortage`).
2. **Maintain** the saga's state in the `OrderState` table.
3. **Decide** what should happen next based on the current state and the incoming event.
4. **Issue Commands** (e.g., `ProcessPaymentCommand`, `ConfirmAvailabilityCommand`) to other services to trigger the
   next step.

This service implements the **Saga Orchestrator** pattern.

## ⚙️ Responsibilities

* Tracks the complete, end-to-end state of an order saga (`OrderState`).
* Logs a complete audit trail of the saga in `OrderStateHistory`.
* Routes commands to the correct microservice via the `OutboxCommand` table (Transactional Outbox).
* Handles both the "happy path" (order completion) and "failure path" (compensating transactions).

## 📥 Kafka Consumers (Topics it Listens To)

This service listens for **Events** from all other services:

* `order-events-topic`: `OrderCreatedEvent`, `OrderCompletedEvent`, `OrderCancelledEvent`
* `payment-events-topic`: `PaymentSucceededEvent`, `PaymentFailedEvent`
* `product-events-topic`: `AvailabilityConfirmedEvent`, `ProductsShortageEvent`, `ProductsUpdatedEvent`
* `shipment-events-topic`: `ShipmentArrangedEvent`, `ArrangementFailedEvent`, `ShipmentCancelledEvent`

## 📤 Kafka Producers (Topics it Publishes To)

This service publishes **Commands** to all other services:

* `order-commands-topic`: `CompleteOrderCommand`, `CancelOrderCommand`
* `payment-commands-topic`: `ProcessPaymentCommand`
* `product-commands-topic`: `ConfirmAvailabilityCommand`, `UpdateProductsCommand`
* `shipment-commands-topic`: `ArrangeShipmentCommand`, `CancelShipmentCommand`

## 🔌 API Endpoints

This service has **no public API**. It is a purely event-driven backend component.

## 🚀 Getting Started

### Prerequisites

* Java (JDK 17+)
* Docker (or a running instance of Kafka & PostgreSQL)

### Running the Service

1. **Configure `application.properties`:**
    * Set the Spring Boot server port.
    * Configure the PostgreSQL database connection (for `OrderState`, `OutboxCommand`, etc.).
    * Configure the Kafka broker connection.
2. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```