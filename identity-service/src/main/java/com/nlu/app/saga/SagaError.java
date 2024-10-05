package com.nlu.app.saga;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatusCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SagaError implements Serializable {
    @Id
    String id;
    String sagaStep;
    String sagaAction;
    String responseBody;
    HttpStatusCode errorCode;
    LocalDateTime createAt;
    String sagaId;
}
