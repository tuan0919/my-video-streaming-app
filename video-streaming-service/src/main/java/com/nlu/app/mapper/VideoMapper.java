package com.nlu.app.mapper;

import com.nlu.app.common.share.event.NewVideoCreatedEvent;
import com.nlu.app.common.share.event.VideoUpvotedEvent;
import com.nlu.app.dto.request.VideoCreationRequest;
import com.nlu.app.dto.response.VideoCreationResponse;
import com.nlu.app.entity.Video;
import com.nlu.app.entity.VideoInteract;
import org.mapstruct.*;

import java.time.LocalDateTime;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface VideoMapper {
    NewVideoCreatedEvent toNewVideoCreatedEvent(Video video);
    @Mapping(target = "thumbnailKey", constant = "DEFAULT-KEY")
    @Mapping(target = "videoDescription", source = "request.description")
    @Mapping(target = "videoName", source = "request.videoName")
    @Mapping(target = "createAt", expression = "java(now())")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "videoKey", source = "key")
    Video toEntity(VideoCreationRequest request, String userId, String key);

    @Mapping(target = "videoURL", source = "videoURL")
    @Mapping(target = "videoName", source = "video.videoName")
    @Mapping(target = "description", source = "video.videoDescription")
    @Mapping(target = "createAt", source = "video.createAt")
    VideoCreationResponse toVideoCreationResponse(Video video, String videoURL);

    default LocalDateTime now() {
        return LocalDateTime.now();
    }
}
