package com.nlu.app.common.share;

public record KafkaMessage(String eventId, String sagaId, String sagaAction, String sagaStep, String sagaStepStatus, String payload) {
}
