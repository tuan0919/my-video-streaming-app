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

    @Mapping(target = "type", constant = "INFO")
    @Mapping(target = "content", qualifiedByName = "notificationContent", source = "event")
    NotificationCreationRequest toRequestDTO(ProfileCreatedEvent event);

    @Mapping(target = "payload", source = "payload", qualifiedByName = "mapToJSON")
    SagaAdvancedRequest toSagaRequest(String sagaId, String sagaAction, String sagaStep, Object payload);

    @Named("notificationContent")
    default String notificationContent(ProfileCreatedEvent event) {
        return String.format("Chào mừng userId %s đến với hệ thống.", event.getUserId());
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
