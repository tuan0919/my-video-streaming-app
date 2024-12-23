package com.nlu.app.mapper;

import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_CommentDTO;
import com.nlu.app.common.share.dto.aggregator_service.response.ClientView_VideoDetailsDTO;
import com.nlu.app.common.share.dto.comment_service.response.CommentResponse;
import com.nlu.app.common.share.dto.identity_service.response.UserResponse;
import com.nlu.app.common.share.dto.profile_service.response.ProfileResponseDTO;
import com.nlu.app.common.share.dto.videoStreaming_service.response.VideoDetailsResponse;
import com.nlu.app.util.MyDateFormat;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface CommentAggregateMapper {
    MyDateFormat formatter = new MyDateFormat();
    @Mappings({
            @Mapping(target = "action", source = "action"),
            @Mapping(target = "comment", expression = "java(toComment(comment))"),
            @Mapping(target = "ownerProfile", expression = "java(toOwnerProfile(ownerIdentity, ownerProfile))")
    })
    ClientView_CommentDTO toDTO(CommentResponse comment,
                                UserResponse ownerIdentity,
                                ProfileResponseDTO ownerProfile,
                                String action);
    @Mapping(target = "createTime", source = "comment.createAt", qualifiedByName = "mapToTime")
    ClientView_CommentDTO.Comment toComment(CommentResponse comment);
    @Mappings({
            @Mapping(target = "userId", source = "ownerIdentity.id"),
    })
    ClientView_CommentDTO.OwnerProfile toOwnerProfile(UserResponse ownerIdentity,
                                                      ProfileResponseDTO profile);

    @Named("mapToTime")
    default String mapToTime(LocalDateTime dateTime) {
        return formatter.relativeToCurrentTime(dateTime);
    }
}
