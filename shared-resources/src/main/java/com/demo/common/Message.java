package com.demo.common;

import com.demo.common.payload.Payload;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Message {

    protected UUID id;

    protected Type type;

    protected String name;

    protected Instant timestamp;

    protected UUID correlationId;

    protected Payload payload;

}
