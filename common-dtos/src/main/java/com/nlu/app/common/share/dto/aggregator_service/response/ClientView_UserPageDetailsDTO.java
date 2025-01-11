package com.nlu.app.common.share.dto.aggregator_service.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientView_UserPageDetailsDTO {
    UserDetails user;
    UserStats stats;
    boolean myself;
    boolean followed;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserDetails {
        String userId;
        String username;
        String fullName;
        String avatar;
        boolean gender;
        String country;
        String address;
        String bio;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserStats {
        int videoCounts;
        int followersCounts;
        int followingCounts;
    }

}
