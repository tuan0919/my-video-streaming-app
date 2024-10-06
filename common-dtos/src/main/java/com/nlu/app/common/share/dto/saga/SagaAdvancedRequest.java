package com.nlu.app.common.share.dto.saga;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.FieldDefaults;

@Builder
@Value
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SagaAdvancedRequest {
    String sagaId;
    String sagaAction;
    String sagaStep;
    String payload;
}
