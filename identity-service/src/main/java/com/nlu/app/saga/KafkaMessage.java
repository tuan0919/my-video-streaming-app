package com.nlu.app.saga;

public record KafkaMessage(String eventId, String sagaId, String sagaAction, String sagaStep, String sagaStepStatus, String payload) {
}
