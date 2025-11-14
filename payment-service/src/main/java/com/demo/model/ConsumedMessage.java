package com.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consumed_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumedMessage {

    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private Instant timestamp;

}
