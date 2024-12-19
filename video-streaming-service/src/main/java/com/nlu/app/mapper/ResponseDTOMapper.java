package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.entity.Video;
import com.nlu.app.entity.VideoInteract;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface ResponseDTOMapper {
    @Mappings({
            @Mapping(target = "videoId", source = "video.videoId"),
            @Mapping(target = "ownerId", source = "video.userId"),
            @Mapping(target = "name", source = "video.videoName"),
            @Mapping(target = "description", source = "video.videoDescription"),
            @Mapping(target = "createAt", source = "video.createAt"),
            @Mapping(target = "viewCount", source = "video.viewCount"),
            @Mapping(target = "progress", source = "interact", qualifiedByName = "progress"),
            @Mapping(target = "action", source = "interact", qualifiedByName = "action"),
            @Mapping(target = "link", source = "videoLink"),
            @Mapping(target = "thumbnail", source = "thumbnail")
    })
    VideoDetailsResponse toResponseDTO(Video video, VideoInteract interact, String videoLink, String thumbnail);

    @Named("action")
    default String action(VideoInteract interact) {
        if (interact == null) return null;
        else
            return interact.getVote();
    }

    @Named("progress")
    default float progress(VideoInteract interact) {
        if (interact == null) return 0f;
        else if (interact.getProgress() == null) return 0f;
        return interact.getProgress();
    }
}
