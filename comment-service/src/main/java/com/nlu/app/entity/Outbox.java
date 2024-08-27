package com.nlu.app.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "outbox")
public class Outbox {
    @Id
    String id = UUID.randomUUID().toString();
    @Field(name = "aggregatetype")
    String aggregateType;
    @Field(name = "aggregateid")
    String aggregateId;
    String type;
    String payload;
}
