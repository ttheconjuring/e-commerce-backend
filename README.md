## Introduction

Tech Stack: <i><b>PostgreSQL, Spring Boot (Gradle), Kafka</b></i>

This is an event-driven system for processing e-commerce orders using a microservice architecture. It features a central
Orchestrator Service that coordinates several other services by sending commands and reacting to their events. The
system
is designed to be resilient, explicitly handling failures like product shortages or failed payments by triggering
compensating actions (rollbacks) to cancel the order and maintain a consistent state.

Orchestrator: `order-saga-orchestrator`

Services: `order-service`, `payment-service`, `product-service`, `shipment-service`, `dlt-service`

--- 

## Setup

You can try the project yourself by following the steps:

...

---

## Main Workflows

### 1. Happy Path

![Happy Path Flowchart]()

#### Order Initiation

1. The CLIENT sends a REQUEST to the Order Service.
2. The Order Service processes the request and sends a RESPONSE back to the CLIENT (confirming the order was received).
3. The Order Service then publishes an `ORDER_CREATED` event to the Order Events Topic.

#### Orchestration Begins

4. The Orchestrator Service is listening to the Order Events Topic and consumes the `ORDER_CREATED` event. This triggers
   its main workflow.
5. The Orchestrator Service sends a `CONFIRM_AVAILABILITY` command to the Product Commands Topic.
6. The Product Service consumes this command, checks its stock, and publishes an `AVAILABILITY_CONFIRMED` event to the
   Product Events Topic.
7. The Orchestrator Service consumes the `AVAILABILITY_CONFIRMED` event.
8. It then sends an `ARRANGE_SHIPMENT` command to the Shipment Commands Topic.
9. The Shipping Service consumes this command, arranges the shipment, and publishes a `SHIPMENT_ARRANGED` event to the
   Shipment Events Topic.
10. The Orchestrator Service consumes the `SHIPMENT_ARRANGED` event.
11. It then sends a `PROCESS_PAYMENT` command to the Payment Commands Topic.
12. The Payment Service consumes this command, processes the payment, and publishes a `PAYMENT_SUCCEEDED` event to the
    Payment Events Topic.
13. The Orchestrator Service consumes the `PAYMENT_SUCCEEDED` event.
14. It then sends an `UPDATE_PRODUCTS` command to the Product Commands Topic (to update the stock levels).
15. The Product Service consumes this command, updates its database, and publishes a `PRODUCTS_UPDATED` event to the
    Product Events Topic.

#### Order Completion

16. The Orchestrator Service consumes the `PRODUCTS_UPDATED` event.
17. It then sends a final `COMPLETE_ORDER` command to the Order Commands Topic.
18. The Order Service consumes this command and publishes a final `ORDER_COMPLETED` event to the Order Events Topic.
19. The Orchestrator Service consumes the `ORDER_COMPLETED` event, and its workflow for this specific order is now
    finished.

---

### 2. Payment Failure

![Payment Failure Flowchart]()

#### Order Initiation (Same as Happy Path)

1. The CLIENT sends a REQUEST to the Order Service, which responds.
2. The Order Service publishes an `ORDER_CREATED` event to the Order Events Topic.

#### Orchestration (Steps 1 & 2)

3. The Orchestrator sends a `CONFIRM_AVAILABILITY` command.
4. The Product Service responds by publishing `AVAILABILITY_CONFIRMED` to the Product Events Topic.
5. The Orchestrator consumes the `AVAILABILITY_CONFIRMED` event. It sends an `ARRANGE_SHIPMENT` command.
6. The Shipping Service responds by publishing `SHIPMENT_ARRANGED` to the Shipment Events Topic.

#### Failure Point: Payment

7. The Orchestrator Service consumes the `SHIPMENT_ARRANGED` event.
8. It sends a `PROCESS_PAYMENT` command to the Payment Commands Topic.
9. The Payment Service consumes this command, but the payment fails.
10. Instead of `PAYMENT_SUCCEEDED`, the Payment Service publishes a `PAYMENT_FAILED` event to the Payment Events Topic.

#### Compensation (Rollback)

11. The Orchestrator consumes the `PAYMENT_FAILED` event.
12. It sends a `CANCEL_SHIPMENT` command to the Shipment Commands Topic.
13. The Shipping Service consumes this command, cancels the shipment, and publishes `SHIPMENT_CANCELLED` to the Shipment
    Events Topic.
14. The Orchestrator consumes the `SHIPMENT_CANCELLED` event. It sends a `CANCEL_ORDER` command to the Order Commands
    Topic.
16. The Order Service consumes this command and publishes a final `ORDER_CANCELLED` event to the Order Events Topic.
17. The Orchestrator Service consumes the `ORDER_CANCELLED` event. The workflow is now complete, and the system is back
    in a consistent state.

---

### 3. Products Shortage

![Product Shortage Flowchart]()

#### Order Initiation

1. The CLIENT sends a REQUEST to the Order Service.
2. The Order Service processes the request, sends a RESPONSE back, and publishes an `ORDER_CREATED` event to the Order
   Events Topic.

#### Orchestration & Failure

3. The Orchestrator Service consumes the `ORDER_CREATED` event and starts its workflow.
4. It sends its first command, `CONFIRM_AVAILABILITY`, to the Product Commands Topic.
5. The Product Service consumes this command, checks the inventory, and discovers the products are not available.
6. Failure Point: The Product Service publishes a `PRODUCTS_SHORTAGE` event to the Product Events Topic.

#### Compensation (Rollback)

7. The Orchestrator Service consumes the PRODUCTS_SHORTAGE event.
8. It immediately sends a `CANCEL_ORDER` command to the Order Commands Topic to reverse the order creation.
9. The Order Service consumes the `CANCEL_ORDER` command and publishes an `ORDER_CANCELLED` event to the Order Events
   Topic.

#### Orchestration Ends

10. The Orchestrator Service consumes the `ORDER_CANCELLED` event, and the workflow for this failed order is now
    complete.

---

### 4. Shipment Arrangement Failure

![Shipment Arrangement Failure Flowchart]()

#### Order Initiation

1. The CLIENT sends a REQUEST to the Order Service, which sends back a RESPONSE.
2. The Order Service publishes an `ORDER_CREATED` event to the Order Events Topic.

#### Orchestration (Step 1)

3. The Orchestrator Service consumes the `ORDER_CREATED` event.
4. It sends a `CONFIRM_AVAILABILITY` command to the Product Commands Topic.
5. The Product Service successfully confirms availability and publishes `AVAILABILITY_CONFIRMED` to the Product Events
   Topic.

#### Failure Point: Shipment

6. The Orchestrator Service consumes the `AVAILABILITY_CONFIRMED` event. It sends an `ARRANGE_SHIPMENT` command to the
   Shipment Commands Topic.
7. The Shipping Service consumes this command but fails to arrange the shipment.
8. It publishes an `ARRANGEMENT_FAILED` event to the Shipment Events Topic.

#### Compensation (Rollback)

9. The orchestrator sends a `CANCEL_ORDER` command to the Order Commands Topic. (Note: In this flow, it only needs to
   cancel the order, as no other state (like payment or product stock) was changed).
10. The Order Service consumes the `CANCEL_ORDER` command and publishes an `ORDER_CANCELLED` event to the Order Events
    Topic.

#### Orchestration Ends

11. The Orchestrator Service consumes the final `ORDER_CANCELLED` event, completing the workflow for this failed order.

---

## Unexpected behaviour

The system has a special tool for handling unexpected behaviour which you can use to identify what and where went wrong.
The tool is called `dlt-service` which is dedicated to collect everything that goes to any dead-letter topic. When an
event
or command is being sent to such a topic, the `dlt-service` consumes it and registers it in its database, making it
available
for later inspection.

If something doesn't work as intended, or you intentionally brake a component and want to see what happened,
you can do this by sending the following request: `GET | http://localhost:8081/error/check`.

---

## Notes

For better and smoother performance of the system, it utilizes some key design patterns to avoid common problems. Here,
I will leave short paragraphs explaining what patterns I used in and how do they work.

#### Saga Orchestration Pattern

Orchestration Saga is a design pattern which is used to coordinate transactions in microservice systems. The pattern
uses a centralized orchestrator to manage the sequence of saga steps.
The main idea of this pattern is to have a service responsible for executing the entire transaction — the Orchestrator.
This service calls on the necessary services in the correct sequence and, in case of an error in one of the services,
knows how to resolve the situation. When services are implemented , we must remember that a service should support
returning to the previous state.

#### The Idempotent Consumer Pattern

An Idempotent Consumer pattern uses a Kafka consumer that can consume the same message any number of times, but only
process it once. To implement the Idempotent Consumer pattern the recommended approach is to add a table to the database
to track processed messages. Each message needs to have a unique messageId assigned by the producing service, either
within the payload, or as a Kafka message header. When a new message is consumed the table is checked for the existence
of this message Id. If present, then the message is a duplicate. The consumer updates its offsets to effectively mark
the message as consumed to ensure it is not redelivered, and no further action takes place.

If the message Id is not present in the table then a database transaction is started and the message Id is inserted. The
message is then processed performing the required business logic. Upon completion the transaction is committed.

#### The Transactional Outbox Pattern

When a message is consumed and processed, resulting in an event being published to Kafka, implementing the Idempotent
Consumer on its own is not sufficient to ensure this will not result in duplicate events being emitted.

While Kafka offers the option of Kafka Transactions, messages published to Kafka in a Kafka transaction are not atomic
with resources written using database transactions. Therefore a failure occurring between a Database transaction being
committed and a Kafka transaction being committed will leave resources in an inconsistent state. Attempting to tie the
commit of the two transactions together using a two-phase commit should also be avoided as this can fail under certain
scenarios leaving the resources in an inconsistent state, adds complexity, and impacts performance by increasing the
latency of all the transactions.

This can be addressed by effectively tying the publishing of an event to the database transaction by using the
Transactional Outbox pattern. A new outbox table is created, and any event that is being published is inserted to this
table.

The write of the outbound event to the outbox table happens as part of the same database transaction that includes the
write of the consumed message Id by the Idempotent Consumer. This means that the consume of the message, any resulting
outbound event publishing, and any other database writes, are all atomic. They either all succeed or all fail. This
provides the strongest guarantee that no duplicate processing will occur.


