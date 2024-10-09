package com.nlu.app.mapper;

import com.nlu.app.common.share.event.VideoUpvotedEvent;
import com.nlu.app.common.share.event.ViewedVideoEvent;
import com.nlu.app.entity.VideoInteract;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface VideoInteractMapper {
    @Mapping(target = "videoId", source = "interact.video.videoId")
    VideoUpvotedEvent toUpVotedEvent(VideoInteract interact);
    @Mapping(target = "videoId", source = "interact.video.videoId")
    ViewedVideoEvent toViewedVideoEvent(VideoInteract interact);
}
