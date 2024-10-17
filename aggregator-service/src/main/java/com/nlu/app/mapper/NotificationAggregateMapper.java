package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_CommentDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_NotificationDTO;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.notification_service.response.NotificationResponse;
import com.nlu.app.util.MyDateFormat;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface NotificationAggregateMapper {
    MyDateFormat formatter = new MyDateFormat();
    @Mappings({
            @Mapping(target = "id", source = "notification.notificationId"),
            @Mapping(target = "content", source = "notification.content"),
            @Mapping(target = "createTime", source = "notification.time", qualifiedByName = "mapToTime"),
    })
    ClientView_NotificationDTO mapToDTO(NotificationResponse notification, String thumbnail, String href, String avatar);

    @Named("mapToTime")
    default String mapToTime(LocalDateTime dateTime) {
        return formatter.relativeToCurrentTime(dateTime);
    }
}
