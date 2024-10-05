package com.nlu.app.saga;

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
public class SagaLog {
    @Id
    String id;
    String sagaStep;
    String sagaAction;
    String result;
    String status;
    @Builder.Default
    LocalDateTime createAt = LocalDateTime.now();
    @Builder.Default
    LocalDateTime updateAt = LocalDateTime.now();
    String sagaId;
}
