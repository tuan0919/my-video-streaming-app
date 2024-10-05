package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.notification_service.request.NotificationCreationRequest;
import com.nlu.app.common.share.event.NotificationCreatedEvent;
import com.nlu.app.constant.NotificationType;
import com.nlu.app.entity.Notification;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface NotificationMapper {

    @Mapping(target = "type", source = "request.type", qualifiedByName = "getEnum")
    Notification mapToEntity(NotificationCreationRequest request);
    NotificationCreatedEvent mapToCreatedEvent(Notification notification);

    @Named("getEnum")
    default NotificationType getByName(String name) {
        return NotificationType.valueOf(name);
    }
}
