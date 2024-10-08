package com.nlu.app.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.SagaAction;
import com.nlu.app.common.share.SagaAdvancedStep;
import com.nlu.app.common.share.SagaCompensationStep;
import com.nlu.app.common.share.SagaStatus;
import com.nlu.app.common.share.event.*;
import com.nlu.app.entity.Outbox;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface OutboxMapper {
    @Mapping(target = "aggregateType", constant = "identity.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.IDENTITY_CREATE)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.userId")
    Outbox toSuccessOutbox(UserCreatedEvent event, String sagaId, String sagaAction);

    @Mapping(target = "aggregateType", constant = "identity.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.IDENTITY_UPDATE)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.userId")
    Outbox toSuccessOutbox(IdentityUpdatedEvent event, String sagaId, String sagaAction);

    @Mapping(target = "aggregateType", constant = "identity.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.ENDING_SAGA)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.sagaId")
    Outbox toSuccessOutbox(SagaCompletedEvent event, String sagaId, String sagaAction);

    @Mapping(target = "aggregateType", constant = "profile.topics")
    @Mapping(target = "sagaId", source = "event.profileId")
    @Mapping(target = "sagaAction", constant = SagaAction.CREATE_NEW_USER)
    @Mapping(target = "sagaStep", constant = SagaCompensationStep.COMPENSATION_PROFILE_CREATE)
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.profileId")
    Outbox toCompenstationOutbox(ProfileRemovedEvent event, String sagaId);

    @Named("mapToJSON")
    default String mapToJSON(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
    }
}
