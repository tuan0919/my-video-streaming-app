package com.nlu.app.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

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
