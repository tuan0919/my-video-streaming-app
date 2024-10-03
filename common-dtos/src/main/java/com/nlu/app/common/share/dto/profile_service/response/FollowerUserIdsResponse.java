package com.nlu.app.common.share.dto.profile_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FollowerUserIdsResponse {
    @Builder.Default
    List<String> followers = new ArrayList<>();
}
