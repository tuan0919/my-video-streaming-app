package com.nlu.app.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.dto.saga.SagaAdvancedRequest;
import com.nlu.app.common.share.event.IdentityUpdatedEvent;
import com.nlu.app.common.share.event.ProfileCreatedEvent;
import com.nlu.app.common.share.event.UserCreatedEvent;
import com.nlu.app.dto.request.UserCreationRequest;
import com.nlu.app.dto.request.UserUpdateRequest;
import com.nlu.app.entity.Role;
import com.nlu.app.entity.User;
import com.nlu.app.exception.ApplicationException;
import com.nlu.app.exception.ErrorCode;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {RoleMapper.class, PermissionMapper.class},
        builder = @Builder(disableBuilder = true))
public interface RequestDTOMapper {

    @Mappings({
            @Mapping(target = "type", constant = "INFO"),
            @Mapping(target = "content", qualifiedByName = "notificationContent", source = "event"),
            @Mapping(target = "title", constant = "Chào mừng đến với hệ thống"),
            @Mapping(target = "relatedObjectId", source = "event.userId"),
            @Mapping(target = "relatedEvent", constant = "NEW_USER_CREATED_EVENT")
    })
    NotificationCreationRequest toRequestDTO(ProfileCreatedEvent event);

    @Mapping(target = "payload", source = "payload", qualifiedByName = "mapToJSON")
    SagaAdvancedRequest toSagaRequest(String sagaId, String sagaAction, String sagaStep, Object payload);

    @Named("notificationContent")
    default String notificationContent(ProfileCreatedEvent event) {
        return String.format("Xin chào %s, chào mừng bạn đến với hệ thống của chúng tôi.", event.getFullName());
    }

    @Named("mapToJSON")
    default String mapToJSON(Object object) {
        if (object == null) return null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new ApplicationException(ErrorCode.UNKNOWN_EXCEPTION);
        }
    }
}
