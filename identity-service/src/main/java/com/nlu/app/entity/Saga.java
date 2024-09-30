package com.nlu.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Saga {
    @Id
    String sagaId;
    String currentStep;
    String sagaAction;
    String state; // PROCESSING, COMPLETED, ABORTED, ABORTING
    LocalDateTime createAt;
    LocalDateTime updateAt;
}
