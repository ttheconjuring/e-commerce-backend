package com.demo.model;

import com.demo.common.payload.Payload;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders_state_history")
@Getter
@NoArgsConstructor
public class OrderStateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id",  nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "status", length = 50, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", updatable = false)
    private Payload payload;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    public OrderStateHistory(OrderState orderState, Payload payload) {
        this.orderId = orderState.getOrderId();
        this.status = orderState.getStatus();
        this.payload = payload;
        this.timestamp = Instant.now();
    }

}
