package com.nlu.app.framework;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SagaState {
    private String id;
    private String payload;
    private Map<String, SagaStepStatus> stepStatus;
    private SagaStatus sagaStatus;

    public void updateStepStatus(String step, SagaStepStatus sagaStepStatus) {
        this.stepStatus.put(step, sagaStepStatus);
    }
}
