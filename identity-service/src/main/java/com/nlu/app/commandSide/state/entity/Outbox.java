package com.nlu.app.commandSide.state.entity;

import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "outbox")
public class Outbox {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id = UUID.randomUUID().toString();

    @Column(name = "aggregatetype")
    String aggregateType;

    @Column(name = "aggregateid")
    String aggregateId;

    String type;
    String payload;
}
