package com.nlu.app.entity;

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
    String sagaAction;
    String sagaStep;
    String sagaStepStatus;
    @Builder.Default
    String sagaId = UUID.randomUUID().toString();
    String payload;
}
