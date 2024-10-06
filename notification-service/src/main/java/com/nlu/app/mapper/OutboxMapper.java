package com.nlu.app.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.ProfileRemovedEvent;
import com.nlu.app.entity.Outbox;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface OutboxMapper {
    @Mapping(target = "aggregateType", constant = "notification.topics")
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.NOTIFICATION_CREATE)
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.notificationId")
    Outbox toSuccessOutbox(NotificationCreatedEvent event, String sagaId, String sagaAction);

    @Named("mapToJSON")
    default String mapToJSON(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
    }
}
