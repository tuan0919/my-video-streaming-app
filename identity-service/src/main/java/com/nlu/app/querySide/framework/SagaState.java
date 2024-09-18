package com.nlu.app.querySide.framework;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

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
