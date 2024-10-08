package com.nlu.app.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.dto.AppResponse;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import com.nlu.app.saga.KafkaMessage;
import com.nlu.app.saga.SagaError;
import org.mapstruct.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface SagaMapper {

    @Mapping(target = "sagaStep", source = "failedMessage.sagaStep")
    @Mapping(target = "sagaAction", source = "failedMessage.sagaAction")
    @Mapping(target = "errorCode", source = "ex.statusCode")
    @Mapping(target = "responseBody", source = "ex", qualifiedByName = "mapException")
    SagaError mapToSagaError(WebClientResponseException ex, KafkaMessage failedMessage);

    @Mapping(target = "sagaStep", source = "failedMessage.sagaStep")
    @Mapping(target = "sagaAction", source = "failedMessage.sagaAction")
    @Mapping(target = "errorCode", expression = "java(INTERNAL_SERVER_ERROR())")
    @Mapping(target = "responseBody", constant = "Something went wrong :(")
    SagaError mapToSagaError(Exception ex, KafkaMessage failedMessage);

    @Named("mapException")
    default String body(WebClientResponseException e) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        var result = e.getResponseBodyAs(AppResponse.class);
        return mapper.writeValueAsString(result.getResult());
    }

    @Named("HttpStatus.INTERNAL_SERVER_ERROR")
    default HttpStatusCode INTERNAL_SERVER_ERROR() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
