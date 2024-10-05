package com.nlu.app.mapper;

import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.saga.KafkaMessage;
import com.nlu.app.saga.SagaError;
import com.nlu.app.saga.SagaLog;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface SagaMapper {
    @Mapping(target = "id", source = "message.eventId")
    @Mapping(target = "status", constant = SagaStatus.SUCCESS)
    SagaLog mapToSuccessLog(KafkaMessage message);

    @Mapping(target = "id", source = "message.eventId")
    @Mapping(target = "status", constant = SagaStatus.FAILED)
    SagaLog mapToFailedLog(KafkaMessage message);

    @Mapping(target = "sagaStep", source = "failedMessage.sagaStep")
    @Mapping(target = "sagaAction", source = "failedMessage.sagaAction")
    @Mapping(target = "errorCode", source = "ex.statusCode")
    @Mapping(target = "responseBody", expression = "java(ex.getResponseBodyAsString())")
    SagaError mapToSagaError(WebClientResponseException ex, KafkaMessage failedMessage);
}
