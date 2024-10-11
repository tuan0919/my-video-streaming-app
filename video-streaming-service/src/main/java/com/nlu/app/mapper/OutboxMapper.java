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
    @Mapping(target = "aggregateType", constant = "video.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.VIDEO_UPVOTE)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.videoId")
    Outbox toSuccessOutbox(VideoUpvotedEvent event, String sagaId, String sagaAction);

    @Mapping(target = "aggregateType", constant = "video.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.VIDEO_DOWNVOTE)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.videoId")
    Outbox toSuccessOutbox(VideoDownVotedEvent event, String sagaId, String sagaAction);

    @Mapping(target = "aggregateType", constant = "video.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.VIDEO_CREATE)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.videoId")
    Outbox toSuccessOutbox(NewVideoCreatedEvent event, String sagaId, String sagaAction);

    @Mapping(target = "aggregateType", constant = "video.topics")
    @Mapping(target = "sagaAction", source = "sagaAction")
    @Mapping(target = "sagaStep", constant = SagaAdvancedStep.FIRST_INTERACT_WITH_VIDEO)
    @Mapping(target = "sagaId", source = "sagaId")
    @Mapping(target = "sagaStepStatus", constant = SagaStatus.SUCCESS)
    @Mapping(target = "payload", source = "event", qualifiedByName = "mapToJSON")
    @Mapping(target = "aggregateId", source = "event.videoId")
    Outbox toSuccessOutbox(ViewedVideoEvent event, String sagaId, String sagaAction);

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
