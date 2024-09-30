package com.nlu.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SagaLog {
    @Id
    String id;
    String sagaStep;
    String sagaAction;
    String result;
    String status;
    LocalDateTime createAt;
    LocalDateTime updateAt;
    String sagaId;
}
