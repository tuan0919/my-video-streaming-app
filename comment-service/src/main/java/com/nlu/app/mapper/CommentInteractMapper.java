package com.nlu.app.mapper;

import com.nlu.app.common.share.event.comment.ReactedToCommentEvent;
import com.nlu.app.entity.CommentInteract;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        builder = @Builder(disableBuilder = true))
public interface CommentInteractMapper {
    ReactedToCommentEvent toReactedToCommentEvent(CommentInteract interact);
}
